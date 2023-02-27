package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import rootheart.codes.common.collections.*
import rootheart.codes.common.strings.splitAndTrimTokens
import rootheart.codes.weatherhistory.database.*
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val log = KotlinLogging.logger {}

@DelicateCoroutinesApi
private val databaseExecutor = CoroutineScope(newFixedThreadPoolContext(32, "database-operations"))

@DelicateCoroutinesApi
private val unzipParseConvertExecutor = CoroutineScope(newFixedThreadPoolContext(8, "download-unzip-parse-convert"))

@DelicateCoroutinesApi
private val downloadThreads = CoroutineScope(newFixedThreadPoolContext(2, "download"))

private val databaseInsertJobs = ArrayList<Job>()

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
            log.info { "Waiting for database insert jobs to complete ..." }
            databaseInsertJobs.joinAll()
        }
    }
    log.info { "Finished import in $duration milliseconds, exiting program" }
}

@OptIn(ExperimentalTime::class)
@DelicateCoroutinesApi
private class MeasurementsImporter(val station: Station, val zippedDataFiles: Collection<ZippedDataFile>) {
    val measurementByTime = ConcurrentHashMap<LocalDate, MeasurementEntity>()

    private val unzipJobs = ArrayList<Job>()
    private val downloadJobs = ArrayList<Job>()

    fun downloadAndConvert() {
        log.info { "Station ${station.id} - Launching for download-unzip-parse-convert jobs" }
        for (zippedDataFile in zippedDataFiles.sortedByDescending(ZippedDataFile::size)) {
            downloadJobs += downloadThreads.launch {
                measureTimedValue { zippedDataFile.url.readBytes() }.apply {
                    log.info {
                        val size = value.size
                        val speed = size * 1_000_000L / duration.inWholeNanoseconds
                        "Station ${station.id} - downloading ${zippedDataFile.fileName} took $duration (${size}bytes, ${speed}kB/s)"
                    }
                    unzipJobs += unzipParseConvertExecutor.launch { unzipAndConvert(zippedDataFile, value) }
                }
            }
        }

        databaseInsertJobs += databaseExecutor.launch {
            log.info { "Station ${station.id} - Waiting for all download jobs to complete" }
            downloadJobs.joinAll()

            log.info { "Station ${station.id} - Waiting for all unzip jobs to complete" }
            unzipJobs.joinAll()

            log.info { "Station ${station.id} - Summarizing ${measurementByTime.size} objects" }
            val summaries = summarize(measurementByTime.values)
            val measurements = measurementByTime.values + summaries

            log.info { "Station ${station.id} - Inserting ${measurements.size} objects into the database" }

            measurements.chunked(1024).parallelStream().forEach(::insertMeasurementsIntoDatabase)
            log.info { "Station ${station.id} - Inserted ${measurements.size} objects into the database" }
        }
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

    private fun unzip(zippedBytes: ByteArray) =
        ZipInputStream(ByteArrayInputStream(zippedBytes)).use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsMeasurementFile(it.name) }) {
                zipInputStream.readBytes()
            } else {
                ByteArray(0)
            }
        }

    private fun parse(bytes: ByteArray) = ByteArrayInputStream(bytes).bufferedReader().use { reader ->
        val header = reader.readLine() ?: ""
        val columnNames = splitAndTrimTokens(header).map { it!! }
        val columnValues = reader.lines().map(::splitAndTrimTokens).collect(Collectors.toList())
        SemicolonSeparatedValues(columnNames, columnValues)
    }


    // TODO introduce an object for hourly data
    // when summarizing, start grouping from there
    private fun convert(semicolonSeparatedValues: SemicolonSeparatedValues, measurementType: MeasurementType) {
        val dateFormatter = if (measurementType == MeasurementType.DAILY) DATE_FORMATTER else DATE_TIME_FORMATTER
        for (row in semicolonSeparatedValues.rows) {
            val measurementTime = dateFormatter.parseLocalDateTime(row[COLUMN_NAME_MEASUREMENT_TIME])
            val day = measurementTime.toLocalDate()
            val hour = measurementTime.hourOfDay
            val measurementRecord = measurementByTime.getOrPut(day) {
                MeasurementEntity(station = station, firstDay = day, interval = Interval.DAY)
            }
            when (measurementType) {
                MeasurementType.AIR_TEMPERATURE   -> setHourlyAirTemperatureData(measurementRecord, hour, row)
                MeasurementType.CLOUD_COVERAGE    -> setHourlyCloudCoverageData(measurementRecord, hour, row)
                MeasurementType.DEW_POINT         -> setHourlyDewPointData(measurementRecord, hour, row)
                MeasurementType.MAX_WIND_SPEED    -> setHourlyMaxWindSpeedData(measurementRecord, hour, row)
                MeasurementType.MOISTURE          -> setHourlyMoistureData(measurementRecord, hour, row)
                MeasurementType.SUNSHINE_DURATION -> setHourlySunshineDurationData(measurementRecord, hour, row)
                MeasurementType.VISIBILITY        -> setHourlyVisibilityData(measurementRecord, hour, row)
                MeasurementType.WIND_SPEED        -> setHourlyWindSpeedData(measurementRecord, hour, row)
                MeasurementType.PRECIPITATION     -> setHourlyPrecipitationData(row, measurementRecord, hour)
                MeasurementType.DAILY             -> setDailyData(measurementRecord, row)
            }
        }

        // TODO fix some data issues
        measurementByTime.values.forEach { m ->
            m.airPressure.min = m.airPressure.details?.nullsafeMin()
            m.airPressure.max = m.airPressure.details?.nullsafeMax()

            m.dewPointTemperature.min = m.dewPointTemperature.details?.nullsafeMin()
            m.dewPointTemperature.avg = m.dewPointTemperature.details?.nullsafeAvgDecimals()
            m.dewPointTemperature.max = m.dewPointTemperature.details?.nullsafeMax()

            m.visibility.min = m.visibility.details?.nullsafeMin()
            m.visibility.avg = m.visibility.details?.nullsafeAvgInts()
            m.visibility.max = m.visibility.details?.nullsafeMax()

            m.humidity.min = m.humidity.details?.nullsafeMin()
            m.humidity.avg = m.humidity.details?.nullsafeAvgDecimals()
            m.humidity.max = m.humidity.details?.nullsafeMax()

            m.sunshine.sum = m.sunshine.details?.filterNotNull()?.sum()

            val histogram = Array(10) { 0 }
            m.cloudCoverage.details
                    ?.filterNotNull()
                    ?.map { if (it == -1) 9 else it }
                    ?.forEach { histogram[it]++ }
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
}

private fun summarize(measurements: Collection<MeasurementEntity>): Collection<MeasurementEntity> {
    val summarizedByMonth = measurements
            .groupBy { LocalDate(it.firstDay.year, it.firstDay.monthOfYear, 1) }
            .mapValues { (beginningOfMonth, measurements) ->
                summarize(beginningOfMonth, measurements, Interval.MONTH)
            }
    val summarizedByYear = summarizedByMonth.values
            .groupBy { LocalDate(it.firstDay.year, 1, 1) }
            .mapValues { (beginningOfYear, measurements) ->
                summarize(beginningOfYear, measurements, Interval.YEAR)
            }
    return summarizedByMonth.values + summarizedByYear.values
}

private fun summarize(
        beginningOfMonth: LocalDate, measurements: List<MeasurementEntity>,
        interval: Interval
): MeasurementEntity {
    val cloudCoverageHistogram = Array(10) { 0 }
    for (m in measurements) {
        m.cloudCoverage.histogram?.forEachIndexed { index, coverage -> cloudCoverageHistogram[index] += coverage }
    }

    return MeasurementEntity(station = measurements[0].station,
                             firstDay = beginningOfMonth,
                             interval = interval,

                             temperature = measurements.minAvgMaxDecimals { it.temperature },
                             dewPointTemperature = measurements.minAvgMaxDecimals { it.dewPointTemperature },
                             humidity = measurements.minAvgMaxDecimals { it.humidity },
                             airPressure = measurements.minAvgMaxDecimals { it.airPressure },
                             visibility = measurements.minAvgMaxInts { it.visibility },
                             windSpeed = measurements.avgMaxDecimals { it.windSpeed },

                             cloudCoverage = Histogram(histogram = cloudCoverageHistogram, details = Array(0) { 0 }),

                             sunshine = measurements.minMaxSumInts { it.sunshine },
                             rainfall = measurements.minMaxSumDecimals { it.rainfall },
                             snowfall = measurements.minMaxSumDecimals { it.snowfall },

                             detailedWindDirectionDegrees = Array(0) { null })
}

private fun setHourlyAirTemperatureData(
        measurementRecord: MeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    measurementRecord.temperature.setHourlyValue(hour, row["TT_TU"]?.let(::BigDecimal))
    measurementRecord.humidity.setHourlyValue(hour, row["RF_TU"]?.let(::BigDecimal))
}

private inline fun <reified N : Number> MinAvgMax<N>.setHourlyValue(hour: Int, value: N?) {
    if (details == null) {
        details = Array(24) { null }
    }
    details!![hour] = value
}

private inline fun <reified N : Number> AvgMax<N>.setHourlyValue(hour: Int, value: N?) {
    if (details == null) {
        details = Array(24) { null }
    }
    details!![hour] = value
}

private inline fun <reified N : Number> MinMaxSumDetails<N>.setHourlyValue(hour: Int, value: N?) {
    if (details == null) {
        details = Array(24) { null }
    }
    details!![hour] = value
}

private fun Histogram.setHourlyValue(hour: Int, value: Int?) {
    if (details == null) {
        details = Array(24) { null }
    }
    details!![hour] = value
}

private fun setHourlyCloudCoverageData(
        measurementRecord: MeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    measurementRecord.cloudCoverage.setHourlyValue(hour, row["V_N"]?.toInt())
}

private fun setHourlyDewPointData(measurementRecord: MeasurementEntity, hour: Int, row: SemicolonSeparatedValues.Row) {
    measurementRecord.dewPointTemperature.setHourlyValue(hour, row["TD"]?.let(::BigDecimal))
}

private fun setHourlyMaxWindSpeedData(
        measurementRecord: MeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    measurementRecord.windSpeed.setHourlyValue(hour, row["FX_911"]?.let(::BigDecimal))
}

private fun setHourlyMoistureData(measurementRecord: MeasurementEntity, hour: Int, row: SemicolonSeparatedValues.Row) {
    measurementRecord.airPressure.setHourlyValue(hour, row["P_STD"]?.let(::BigDecimal))
}

private fun setHourlySunshineDurationData(
        measurementRecord: MeasurementEntity, hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    measurementRecord.sunshine.setHourlyValue(hour, row["SD_SO"]?.let(::BigDecimal)?.intValueExact())
}

private fun setHourlyVisibilityData(
        measurementRecord: MeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    measurementRecord.visibility.setHourlyValue(hour, row["V_VV"]?.toInt())
}

private fun setHourlyWindSpeedData(measurementRecord: MeasurementEntity, hour: Int, row: SemicolonSeparatedValues.Row) {
    measurementRecord.windSpeed.setHourlyValue(hour, row["F"]?.let(::BigDecimal))
    measurementRecord.detailedWindDirectionDegrees?.set(hour, row["D"]?.toInt())
}

private fun setHourlyPrecipitationData(
        row: SemicolonSeparatedValues.Row,
        measurementRecord: MeasurementEntity,
        hour: Int
) {
    val precipitationTypeCodeString = row["WRTR"]
    if (precipitationTypeCodeString == "6") {
        measurementRecord.rainfall.setHourlyValue(hour, row["R1"]?.let(::BigDecimal))
    } else if (precipitationTypeCodeString == "7") {
        measurementRecord.snowfall.setHourlyValue(hour, row["R1"]?.let(::BigDecimal))
    }
}

private fun setDailyData(measurementRecord: MeasurementEntity, row: SemicolonSeparatedValues.Row) {
    measurementRecord.windSpeed.max = row["FX"]?.let(::BigDecimal)
    measurementRecord.windSpeed.avg = row["FM"]?.let(::BigDecimal)
    val precipitationType = row["RSKF"]
    if (precipitationType == "6") {
        measurementRecord.rainfall.sum = row["RSK"]?.let(::BigDecimal)
    } else if (precipitationType == "7") {
        measurementRecord.snowfall.sum = row["RSK"]?.let(::BigDecimal)
    }
    // measurementRecord.sumSunshineDurationMinutes = row["SDK"]?.let(::BigDecimal)?.multiply(sixty)?.intValueExact()
    // measurementRecord.snowheightCentimeters = row["SHK"]?.let(::BigDecimal)
    measurementRecord.airPressure.avg = row["PM"]?.let(::BigDecimal)
    measurementRecord.temperature.avg = row["TMK"]?.let(::BigDecimal)
    measurementRecord.humidity.avg = row["UPM"]?.let(::BigDecimal)
    measurementRecord.temperature.max = row["TXK"]?.let(::BigDecimal)
    measurementRecord.temperature.min = row["TNK"]?.let(::BigDecimal)
}

//private fun summarizeDecimals(
//        measurements: Collection<MeasurementJson>,
//        selector: (MeasurementJson) -> MinAvgMax<BigDecimal?>
//) =
//    MinAvgMax<BigDecimal?>(min = measurements.nullsafeMin { selector(it).min },
//                           avg = measurements.nullsafeAvgDecimal { selector(it).avg },
//                           max = measurements.nullsafeMax { selector(it).max },
//                           details = measurements.map { selector(it).avg }.toTypedArray()
//    )
//
//private fun summarizeIntegers(
//        measurements: Collection<MeasurementJson>,
//        selector: (MeasurementJson) -> MinAvgMax<Int?>
//) =
//    MinAvgMax<Int?>(min = measurements.nullsafeMin { selector(it).min },
//                    avg = measurements.nullsafeAvgInt { selector(it).avg },
//                    max = measurements.nullsafeMax { selector(it).max },
//                    details = measurements.map { selector(it).avg }.toTypedArray())
//
//private const val DETAILS_SIZE = 24

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

