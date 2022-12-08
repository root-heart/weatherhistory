package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import rootheart.codes.common.collections.nullsafeAvgDecimal
import rootheart.codes.common.collections.nullsafeAvgDecimals
import rootheart.codes.common.collections.nullsafeAvgInt
import rootheart.codes.common.collections.nullsafeAvgInts
import rootheart.codes.common.collections.nullsafeMax
import rootheart.codes.common.collections.nullsafeMin
import rootheart.codes.common.collections.nullsafeSumDecimals
import rootheart.codes.common.collections.nullsafeSumInts
import rootheart.codes.common.strings.splitAndTrimTokens
import rootheart.codes.weatherhistory.database.Decimals
import rootheart.codes.weatherhistory.database.DecimalsColumns
import rootheart.codes.weatherhistory.database.Histogram
import rootheart.codes.weatherhistory.database.Integers
import rootheart.codes.weatherhistory.database.IntegersColumns
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.Measurement
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.MinAvgMax
import rootheart.codes.weatherhistory.database.MinAvgMaxColumns
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.StationsTable
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import java.util.zip.ZipInputStream
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val log = KotlinLogging.logger {}

@DelicateCoroutinesApi
private val databaseExecutor = CoroutineScope(newFixedThreadPoolContext(2, "database-operations"))

@DelicateCoroutinesApi
private val unzipParseConvertExecutor = CoroutineScope(newFixedThreadPoolContext(8, "download-unzip-parse-convert"))

@DelicateCoroutinesApi
private val downloadThreads = CoroutineScope(newFixedThreadPoolContext(8, "download"))

private val jobs = ArrayList<Job>()

private val sixty = BigDecimal(60)

@DelicateCoroutinesApi
fun importMeasurements(hourlyDirectory: HtmlDirectory, dailyDirectory: HtmlDirectory) {
    val stationByExternalId = StationDao.findAll().associateBy(Station::externalId)
    val duration = measureTimeMillis {
        runBlocking {
            val data = HashMap<Station, MutableList<ZippedDataFile>>()

            hourlyDirectory.getAllZippedDataFiles()
                .groupBy { it.externalId }
                .mapKeys { stationByExternalId[it.key] }
                .filter { it.key != null }
                .forEach { (station, dataFiles) -> data[station!!] = ArrayList(dataFiles) }

            dailyDirectory.getAllZippedDataFiles()
                .groupBy { it.externalId }
                .mapKeys { stationByExternalId[it.key] }
                .filter { it.key != null }
                .forEach { (station, dataFiles) ->
                    if (!data.containsKey(station)) {
                        data[station!!] = ArrayList(dataFiles)
                    } else {
                        data[station!!]!!.addAll(dataFiles)
                    }
                }

            data.map { MeasurementsImporter(it.key, it.value) }
                .forEach { it.downloadAndConvert() }
            log.info { "Waiting for database jobs to complete ..." }
            jobs.joinAll()
        }
    }
    log.info { "Finished import in $duration milliseconds, exiting program" }
}

@OptIn(ExperimentalTime::class)
@DelicateCoroutinesApi
private class MeasurementsImporter(val station: Station, val zippedDataFiles: Collection<ZippedDataFile>) {
    val measurementByTime = ConcurrentHashMap<LocalDate, Measurement>()

    fun downloadAndConvert() {
        log.info { "Station ${station.id} - Launching for download-unzip-parse-convert jobs" }
        val unzipJobs = ArrayList<Job>()
        val downloadJobs = zippedDataFiles
            .sortedByDescending(ZippedDataFile::size)
            .map {
                downloadThreads.launch {
                    val timedValue = measureTimedValue { it.url.readBytes() }
                    log.info { "Station ${station.id}, downloading ${timedValue.value.size} bytes from ${it.fileName} took ${timedValue.duration} millis" }
                    unzipJobs += unzipParseConvertExecutor.launch { unzipAndConvert(it, timedValue.value) }
                }
            }

        jobs += databaseExecutor.launch {
            log.info { "Station ${station.id} - Waiting for download-unzip-parse-convert jobs to complete" }
            downloadJobs.joinAll()
            unzipJobs.joinAll()
            val monthlySummaries = summarize()

//            MeasurementImporter.importEntities(measurementByTime.values)
//            MeasurementImporter.importEntities(monthlySummaries.values)

            val measurements = measurementByTime.values

            transaction {
                val stationIds = measurements.mapNotNull { it.stationId }.distinct()
                val query = StationsTable.select { StationsTable.id.inList(stationIds) }
                val stationIdById = query.map { row -> row[StationsTable.id] }.associateBy { it.value }

                measurements.chunked(1000).forEach { chunk ->
                    MeasurementsTable.batchInsert(chunk) {

                        this[MeasurementsTable.stationId] = stationIdById[it.stationId]!!
                        this[MeasurementsTable.firstDay] = it.firstDayDateTime
                        this[MeasurementsTable.interval] = it.interval

                        copyMinAvgMax(it.temperatures, MeasurementsTable.temperatures)
                        copyMinAvgMax(it.dewPointTemperatures, MeasurementsTable.dewPointTemperatures)
                        copyMinAvgMax(it.humidity, MeasurementsTable.humidity)
                        copyMinAvgMax(it.airPressure, MeasurementsTable.airPressure)
                        copyMinAvgMax(it.visibility, MeasurementsTable.visibility)

                        this[MeasurementsTable.cloudCoverage.histogram] = it.cloudCoverage.histogram
                        this[MeasurementsTable.cloudCoverage.details] = it.cloudCoverage.details

                        copySum(it.sunshineDuration, MeasurementsTable.sunshineDuration)
                        copySum(it.rainfall, MeasurementsTable.rainfall)
                        copySum(it.snowfall, MeasurementsTable.snowfall)
                        copySum(it.sunshineDuration, MeasurementsTable.sunshineDuration)

                        this[MeasurementsTable.detailedWindDirectionDegrees] = it.detailedWindDirectionDegrees
                    }
                }
            }
        }
    }

    private fun <N : Number> BatchInsertStatement.copyMinAvgMax(from: MinAvgMax<N?>, to: MinAvgMaxColumns<N>) {
        this[to.min] = from.min
        this[to.avg] = from.avg
        this[to.max] = from.max
        this[to.details] = from.details
    }

    private fun BatchInsertStatement.copySum(from: Integers, to: IntegersColumns) {
        this[to.sum] = from.sum
        this[to.details] = from.values
    }

    private fun BatchInsertStatement.copySum(from: Decimals, to: DecimalsColumns) {
        this[to.sum] = from.sum
        this[to.details] = from.values
    }

    private fun unzipAndConvert(zippedDataFile: ZippedDataFile, zippedBytes: ByteArray) {
        val durationAndRowCount = measureTimedValue {
            val unzippedBytes = unzip(zippedBytes)
            val parsed = parse(unzippedBytes)
            convert(parsed, zippedDataFile.measurementType)
            return@measureTimedValue parsed.rows.size
        }
        log.debug { "Station ${station.id}, file ${zippedDataFile.fileName} - unzipping ${zippedBytes.size} bytes and converting them to ${durationAndRowCount.value} rows took ${durationAndRowCount.duration} millis" }
    }

    private fun unzip(zippedBytes: ByteArray): ByteArray =
        ZipInputStream(ByteArrayInputStream(zippedBytes)).use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsMeasurementFile(it.name) }) {
                return@use zipInputStream.readBytes()
            } else {
                return@use ByteArray(0)
            }
        }

    fun parse(bytes: ByteArray): SemicolonSeparatedValues =
        ByteArrayInputStream(bytes).bufferedReader().use { reader ->
            val header = reader.readLine() ?: ""
            val columnNames = splitAndTrimTokens(header).map { it!! }
            val columnValues = reader.lines().map(::splitAndTrimTokens).collect(Collectors.toList())
            return SemicolonSeparatedValues(columnNames, columnValues)
        }

    private fun convert(semicolonSeparatedValues: SemicolonSeparatedValues, measurementType: MeasurementType) {
        val indexMeasurementTime = semicolonSeparatedValues.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        if (measurementType == MeasurementType.DAILY) {
            for (row in semicolonSeparatedValues.rows) {
                val day = DATE_FORMATTER.parseLocalDate(row[indexMeasurementTime])
                val measurementRecord = measurementByTime.getOrPut(day) {
                    emptyMeasurement(station, day, Interval.DAY, 24)
                }
                var columnIndex = semicolonSeparatedValues.columnNames.indexOf("FX")
                measurementRecord.wind.max = row[columnIndex]?.let(::BigDecimal)
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("FM")
                measurementRecord.wind.avg = row[columnIndex]?.let(::BigDecimal)
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("RSKF")
                val precipitationType = row[columnIndex]
                if (precipitationType == "6") {
                    columnIndex = semicolonSeparatedValues.columnNames.indexOf("RSK")
                    measurementRecord.rainfall.sum = row[columnIndex]?.let(::BigDecimal)
                } else if (precipitationType == "7") {
                    columnIndex = semicolonSeparatedValues.columnNames.indexOf("RSK")
                    measurementRecord.snowfall.sum = row[columnIndex]?.let(::BigDecimal)
                }
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("SDK")
//                measurementRecord.sumSunshineDurationMinutes = row[columnIndex]?.let(::BigDecimal)?.multiply(sixty)?.intValueExact()
//                    columnIndex = semicolonSeparatedValues.columnNames.indexOf("SHK")
//                    measurementRecord.snowheightCentimeters = row[columnIndex]?.let(::BigDecimal)
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("PM")
                measurementRecord.airPressure.avg = row[columnIndex]?.let(::BigDecimal)
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("TMK")
                measurementRecord.temperatures.avg = row[columnIndex]?.let(::BigDecimal)
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("UPM")
                measurementRecord.humidity.avg = row[columnIndex]?.let(::BigDecimal)
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("TXK")
                measurementRecord.temperatures.max = row[columnIndex]?.let(::BigDecimal)
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("TNK")
                measurementRecord.temperatures.min = row[columnIndex]?.let(::BigDecimal)
            }
        } else {
            for (row in semicolonSeparatedValues.rows) {
                val measurementTime = DATE_TIME_FORMATTER.parseLocalDateTime(row[indexMeasurementTime])
                val day = measurementTime.toLocalDate()
                val hour = measurementTime.hourOfDay
                val measurementRecord = measurementByTime.getOrPut(day) {
                    emptyMeasurement(station, day, Interval.DAY, 24)
                }
                when (measurementType) {
                    MeasurementType.AIR_TEMPERATURE -> {
                        val idxAirTemp = semicolonSeparatedValues.columnNames.indexOf("TT_TU")
                        val idxHumidity = semicolonSeparatedValues.columnNames.indexOf("RF_TU")
                        measurementRecord.temperatures.details[hour] =
                            nullsafeBigDecimal(row[idxAirTemp])
                        measurementRecord.humidity.details[hour] = nullsafeBigDecimal(row[idxHumidity])
                    }

                    MeasurementType.CLOUDINESS -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("V_N")
                        measurementRecord.cloudCoverage.details[hour] = nullsafeInt(row[columnIndex])
                    }

                    MeasurementType.DEW_POINT -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("TD")
                        measurementRecord.dewPointTemperatures.details[hour] =
                            row[columnIndex]?.let(::BigDecimal)
                    }

                    MeasurementType.MAX_WIND_SPEED -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("FX_911")
                        measurementRecord.wind.details[hour] =
                            row[columnIndex]?.let(::BigDecimal)
                    }

                    MeasurementType.MOISTURE -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("P_STD")
                        measurementRecord.airPressure.details[hour] =
                            row[columnIndex]?.let(::BigDecimal)
                    }

                    MeasurementType.SUNSHINE_DURATION -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("SD_SO")
                        measurementRecord.sunshineDuration.values[hour] =
                            row[columnIndex]?.let(::BigDecimal)?.intValueExact()
                    }

                    MeasurementType.VISIBILITY -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("V_VV")
                        measurementRecord.visibility.details[hour] = nullsafeInt(row[columnIndex])
                    }

                    MeasurementType.WIND_SPEED -> {
                        var columnIndex = semicolonSeparatedValues.columnNames.indexOf("F")
                        measurementRecord.wind.details[hour] =
                            row[columnIndex]?.let(::BigDecimal)
                        columnIndex = semicolonSeparatedValues.columnNames.indexOf("D")
                        measurementRecord.detailedWindDirectionDegrees[hour] = nullsafeInt(row[columnIndex])
                    }

                    MeasurementType.PRECIPITATION -> {
                        var columnIndex = semicolonSeparatedValues.columnNames.indexOf("WRTR")
                        val precipitationTypeCodeString = row[columnIndex]
                        if (precipitationTypeCodeString == "6") {
                            columnIndex = semicolonSeparatedValues.columnNames.indexOf("R1")
                            measurementRecord.rainfall.values[hour] =
                                row[columnIndex]?.let(::BigDecimal)
                        } else if (precipitationTypeCodeString == "7") {
                            columnIndex = semicolonSeparatedValues.columnNames.indexOf("R1")
                            measurementRecord.snowfall.values[hour] =
                                row[columnIndex]?.let(::BigDecimal)
                        }
                    }

                    else -> throw Exception("AAAAAAAAAAAAA")
                }
            }
        }

        // TODO fix some data issues
        measurementByTime.values.forEach { m ->
            m.airPressure.min = m.airPressure.details.nullsafeMin()
            m.airPressure.max = m.airPressure.details.nullsafeMax()

            m.dewPointTemperatures.min = m.dewPointTemperatures.details.nullsafeMin()
            m.dewPointTemperatures.avg = m.dewPointTemperatures.details.nullsafeAvgDecimals()
            m.dewPointTemperatures.max = m.dewPointTemperatures.details.nullsafeMax()

            m.visibility.min = m.visibility.details.nullsafeMin()
            m.visibility.avg = m.visibility.details.nullsafeAvgInts()
            m.visibility.max = m.visibility.details.nullsafeMax()

            m.humidity.min = m.humidity.details.nullsafeMin()
            m.humidity.avg = m.humidity.details.nullsafeAvgDecimals()
            m.humidity.max = m.humidity.details.nullsafeMax()

            m.sunshineDuration.sum = m.sunshineDuration.values.filterNotNull().sum()

            val histogram = Array(10) { 0 }
            m.cloudCoverage.details
                .filterNotNull()
                .map { if (it == -1) 9 else it }
                .forEach { histogram[it]++ }
            m.cloudCoverage.histogram = histogram
        }

//        val list = measurementByTime.values.sortedBy { it.day }
//        for ((index, measurement) in list.withIndex()) {
//            if (measurement.precipitationMillimeters != null
//                && measurement.precipitationMillimeters!! > BigDecimal.ZERO
//                && measurement.precipitationType == null
//            ) {
//                if (index > 0) {
//                    if (list[index - 1].precipitationType != null) {
//                        measurement.precipitationType = list[index - 1].precipitationType
//                    } else if (index < list.size - 1) {
//                        measurement.precipitationType = list[index + 1].precipitationType
//                    }
//                }
//            }
//        }

    }

    fun summarize(): Map<LocalDate, Measurement> = measurementByTime.values
        .groupBy { LocalDate(it.firstDay.year, it.firstDay.monthOfYear, 1) }
        .mapValues { (beginningOfMonth, measurements) -> mapMeasurement(beginningOfMonth, measurements) }

    fun mapMeasurement(beginningOfMonth: LocalDate, measurements: List<Measurement>): Measurement {
        val cloudCoverageHistogram = Array(10) { 0 }
        for (m in measurements) {
            m.cloudCoverage.histogram.forEachIndexed { index, coverage ->
                cloudCoverageHistogram[index] += coverage ?: 0
            }
        }

        return Measurement(
            station = measurements[0].station,
            firstDay = beginningOfMonth,
            interval = Interval.MONTH,

            temperatures = summarizeDecimals(measurements, Measurement::temperatures),
            dewPointTemperatures = summarizeDecimals(measurements, Measurement::dewPointTemperatures),
            humidity = summarizeDecimals(measurements, Measurement::humidity),
            airPressure = summarizeDecimals(measurements, Measurement::airPressure),
            visibility = summarizeIntegers(measurements, Measurement::visibility),
            wind = summarizeDecimals(measurements, Measurement::wind),

            cloudCoverage = Histogram(histogram = cloudCoverageHistogram, details = Array(0) { 0 }),

            sunshineDuration = Integers(
                sum = measurements.nullsafeSumInts { it.sunshineDuration.sum },
                values = measurements.flatMap { it.sunshineDuration.values.toList() }.toTypedArray()
            ),

            rainfall = Decimals(
                sum = measurements.nullsafeSumDecimals { it.rainfall.sum },
                values = measurements.flatMap { it.rainfall.values.toList() }.toTypedArray()
            ),

            snowfall = Decimals(
                sum = measurements.nullsafeSumDecimals { it.snowfall.sum },
                values = measurements.flatMap { it.snowfall.values.toList() }.toTypedArray()
            )
        )
    }

    private fun summarizeDecimals(
        measurements: Collection<Measurement>,
        selector: (Measurement) -> MinAvgMax<BigDecimal?>
    ) = MinAvgMax<BigDecimal?>(
        min = measurements.nullsafeMin { selector(it).min },
        avg = measurements.nullsafeAvgDecimal { selector(it).avg },
        max = measurements.nullsafeMax { selector(it).max },
        details = measurements.map { selector(it).avg }.toTypedArray()
    )

    private fun summarizeIntegers(
        measurements: Collection<Measurement>,
        selector: (Measurement) -> MinAvgMax<Int?>
    ) = MinAvgMax<Int?>(
        min = measurements.nullsafeMin { selector(it).min },
        avg = measurements.nullsafeAvgInt { selector(it).avg },
        max = measurements.nullsafeMax { selector(it).max },
        details = measurements.map { selector(it).avg }.toTypedArray()
    )
}

private fun emptyMeasurement(station: Station, day: LocalDate, interval: Interval, detailsSize: Int): Measurement {
    return Measurement(
        station = station,
        firstDay = day,
        interval = interval,
        temperatures = MinAvgMax(details = Array(detailsSize) { null }),
        dewPointTemperatures = MinAvgMax(details = Array(detailsSize) { null }),
        airPressure = MinAvgMax(details = Array(detailsSize) { null }),
        humidity = MinAvgMax(details = Array(detailsSize) { null }),
        wind = MinAvgMax(details = Array(detailsSize) { null }),
        cloudCoverage = Histogram(histogram = Array(10) { 0 }, details = Array(detailsSize) { null }),
        visibility = MinAvgMax(details = Array(detailsSize) { null }),
        rainfall = Decimals(values = Array(detailsSize) { null }),
        snowfall = Decimals(values = Array(detailsSize) { null }),
        sunshineDuration = Integers(values = Array(detailsSize) { null })
    )
}

private fun fileIsMeasurementFile(filename: String) =
    filename.startsWith("produkt_") && filename.endsWith(".txt")

private fun nullsafeBigDecimal(value: String?): BigDecimal? {
    if (value != null) {
        return BigDecimal(value)
    }
    return null
}

private fun nullsafeInt(value: String?): Int? {
    if (value != null) {
        return Integer.parseInt(value)
    }
    return null
}

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC()
private val DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC()

private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"

inline fun Array<BigDecimal?>.nullsafeAvgDecimal(): BigDecimal? =
    this.nullsafeSumDecimals { it }?.let { it / BigDecimal(size) }
