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
import rootheart.codes.common.collections.nullsafeAvg
import rootheart.codes.common.collections.nullsafeMax
import rootheart.codes.common.collections.nullsafeMin
import rootheart.codes.common.collections.nullsafeSum
import rootheart.codes.common.strings.splitAndTrimTokens
import rootheart.codes.weatherhistory.database.Measurement
import rootheart.codes.weatherhistory.database.MeasurementImporter
import rootheart.codes.weatherhistory.database.MonthlySummary
import rootheart.codes.weatherhistory.database.MonthlySummaryImporter
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
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
            MeasurementImporter.importEntities(measurementByTime.values)
            MonthlySummaryImporter.importEntities(monthlySummaries.values)
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

    private fun convert(
        semicolonSeparatedValues: SemicolonSeparatedValues,
        measurementType: MeasurementType
    ) {
        val indexMeasurementTime = semicolonSeparatedValues.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        if (measurementType == MeasurementType.DAILY) {
            for (row in semicolonSeparatedValues.rows) {
                val day = DATE_FORMATTER.parseLocalDate(row[indexMeasurementTime])
                val measurementRecord = measurementByTime.getOrPut(day) {
                    Measurement(station = station, day = day)
                }
                var columnIndex = semicolonSeparatedValues.columnNames.indexOf("FX")
                measurementRecord.maxWindSpeedMetersPerSecond = nullsafeBigDecimal(row[columnIndex])
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("FM")
                measurementRecord.avgWindSpeedMetersPerSecond = nullsafeBigDecimal(row[columnIndex])
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("RSKF")
                val precipitationType = row[columnIndex]
                if (precipitationType == "6") {
                    columnIndex = semicolonSeparatedValues.columnNames.indexOf("RSK")
                    measurementRecord.sumRainfallMillimeters = nullsafeBigDecimal(row[columnIndex])
                } else if (precipitationType == "7") {
                    columnIndex = semicolonSeparatedValues.columnNames.indexOf("RSK")
                    measurementRecord.sumSnowfallMillimeters = nullsafeBigDecimal(row[columnIndex])
                }
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("SDK")
                measurementRecord.sumSunshineDurationHours = nullsafeBigDecimal(row[columnIndex])
//                    columnIndex = semicolonSeparatedValues.columnNames.indexOf("SHK")
//                    measurementRecord.snowheightCentimeters = nullsafeBigDecimal(row[columnIndex])
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("PM")
                measurementRecord.avgAirPressureHectopascals = nullsafeBigDecimal(row[columnIndex])
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("TMK")
                measurementRecord.avgAirTemperatureCentigrade = nullsafeBigDecimal(row[columnIndex])
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("UPM")
                measurementRecord.avgHumidityPercent = nullsafeBigDecimal(row[columnIndex])
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("TXK")
                measurementRecord.maxAirTemperatureCentigrade = nullsafeBigDecimal(row[columnIndex])
                columnIndex = semicolonSeparatedValues.columnNames.indexOf("TNK")
                measurementRecord.minAirTemperatureCentigrade = nullsafeBigDecimal(row[columnIndex])
            }
        } else {
            for (row in semicolonSeparatedValues.rows) {
                val measurementTime = DATE_TIME_FORMATTER.parseLocalDateTime(row[indexMeasurementTime])
                val day = measurementTime.toLocalDate()
                val hour = measurementTime.hourOfDay
                val measurementRecord = measurementByTime.getOrPut(day) {
                    Measurement(station = station, day = day)
                }
                when (measurementType) {
                    MeasurementType.AIR_TEMPERATURE -> {
                        val idxAirTemp = semicolonSeparatedValues.columnNames.indexOf("TT_TU")
                        val idxHumidity = semicolonSeparatedValues.columnNames.indexOf("RF_TU")
                        measurementRecord.hourlyAirTemperatureCentigrade[hour] = nullsafeBigDecimal(row[idxAirTemp])
                        measurementRecord.hourlyHumidityPercent[hour] = nullsafeBigDecimal(row[idxHumidity])
                    }
                    MeasurementType.CLOUDINESS -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("V_N")
                        measurementRecord.hourlyCloudCoverages[hour] = nullsafeInt(row[columnIndex])
                    }
                    MeasurementType.DEW_POINT -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("TD")
                        measurementRecord.hourlyDewPointTemperatureCentigrade[hour] =
                            nullsafeBigDecimal(row[columnIndex])
                    }
                    MeasurementType.MAX_WIND_SPEED -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("FX_911")
                        measurementRecord.hourlyWindSpeedMetersPerSecond[hour] = nullsafeBigDecimal(row[columnIndex])
                    }
                    MeasurementType.MOISTURE -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("P_STD")
                        measurementRecord.hourlyAirPressureHectopascals[hour] = nullsafeBigDecimal(row[columnIndex])
                    }
                    MeasurementType.SUNSHINE_DURATION -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("SD_SO")
                        measurementRecord.hourlySunshineDurationMinutes[hour] =
                            nullsafeBigDecimal(row[columnIndex])?.intValueExact()
                    }
                    MeasurementType.VISIBILITY -> {
                        val columnIndex = semicolonSeparatedValues.columnNames.indexOf("V_VV")
                        measurementRecord.hourlyVisibilityMeters[hour] = nullsafeInt(row[columnIndex])
                    }
                    MeasurementType.WIND_SPEED -> {
                        var columnIndex = semicolonSeparatedValues.columnNames.indexOf("F")
                        measurementRecord.hourlyWindSpeedMetersPerSecond[hour] = nullsafeBigDecimal(row[columnIndex])
                        columnIndex = semicolonSeparatedValues.columnNames.indexOf("D")
                        measurementRecord.hourlyWindDirectionDegrees[hour] = nullsafeInt(row[columnIndex])
                    }
                    MeasurementType.PRECIPITATION -> {
                        var columnIndex = semicolonSeparatedValues.columnNames.indexOf("WRTR")
                        val precipitationTypeCodeString = row[columnIndex]
                        if (precipitationTypeCodeString == "6") {
                            columnIndex = semicolonSeparatedValues.columnNames.indexOf("R1")
                            measurementRecord.hourlyRainfallMillimeters[hour] = nullsafeBigDecimal(row[columnIndex])
                        } else if (precipitationTypeCodeString == "7") {
                            columnIndex = semicolonSeparatedValues.columnNames.indexOf("R1")
                            measurementRecord.hourlySnowfallMillimeters[hour] = nullsafeBigDecimal(row[columnIndex])
                        }
                    }
                }
            }
        }

        // TODO fix some data issues
        measurementByTime.values.forEach { m ->
            m.minAirPressureHectopascals = m.hourlyAirPressureHectopascals.nullsafeMin()
            m.maxAirPressureHectopascals = m.hourlyAirPressureHectopascals.nullsafeMax()

            m.minDewPointTemperatureCentigrade = m.hourlyDewPointTemperatureCentigrade.nullsafeMin()
            m.avgDewPointTemperatureCentigrade = m.hourlyDewPointTemperatureCentigrade.nullsafeAvg()
            m.maxDewPointTemperatureCentigrade = m.hourlyDewPointTemperatureCentigrade.nullsafeMax()

            m.minVisibilityMeters = m.hourlyVisibilityMeters.nullsafeMin()
            m.avgVisibilityMeters = m.hourlyVisibilityMeters.nullsafeAvg()
            m.maxVisibilityMeters = m.hourlyVisibilityMeters.nullsafeMax()
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

        // TODO incorporate daily measurements for min, avg, max and sum values
    }

    fun summarize(): Map<LocalDate, MonthlySummary> = measurementByTime.values
        .groupBy { LocalDate(it.day.year, it.day.monthOfYear, 1) }
        .mapValues { (beginningOfMonth, measurements) ->
            val cloudCoverageHistogram = Array(10) { 0 }
            for (m in measurements) {
                for (c in m.hourlyCloudCoverages) {
                    if (c != null) {
                        if (c == -1) cloudCoverageHistogram[9]++
                        else cloudCoverageHistogram[c]++
                    }
                }
            }
            MonthlySummary(
                station = measurements[0].station,
                year = beginningOfMonth.year,
                month = beginningOfMonth.monthOfYear,
                minAirTemperatureCentigrade = measurements.nullsafeMin(Measurement::minAirTemperatureCentigrade),
                avgAirTemperatureCentigrade = measurements.nullsafeAvg(Measurement::avgAirTemperatureCentigrade),
                maxAirTemperatureCentigrade = measurements.nullsafeMax(Measurement::maxAirTemperatureCentigrade),
                minDewPointTemperatureCentigrade = measurements.nullsafeMin(Measurement::minDewPointTemperatureCentigrade),
                avgDewPointTemperatureCentigrade = measurements.nullsafeAvg(Measurement::avgDewPointTemperatureCentigrade),
                maxDewPointTemperatureCentigrade = measurements.nullsafeMax(Measurement::maxDewPointTemperatureCentigrade),
                minHumidityPercent = measurements.nullsafeMin(Measurement::minHumidityPercent),
                avgHumidityPercent = measurements.nullsafeAvg(Measurement::avgHumidityPercent),
                maxHumidityPercent = measurements.nullsafeMax(Measurement::maxHumidityPercent),
                minAirPressureHectopascals = measurements.nullsafeMin(Measurement::minAirPressureHectopascals),
                avgAirPressureHectopascals = measurements.nullsafeAvg(Measurement::avgAirPressureHectopascals),
                maxAirPressureHectopascals = measurements.nullsafeMax(Measurement::maxAirPressureHectopascals),
                minVisibilityMeters = measurements.nullsafeMin(Measurement::minVisibilityMeters),
                avgVisibilityMeters = measurements.nullsafeAvg(Measurement::avgVisibilityMeters),
                maxVisibilityMeters = measurements.nullsafeMax(Measurement::maxVisibilityMeters),
                cloudCoverageHistogram = cloudCoverageHistogram,
                sumSunshineDurationHours = measurements.nullsafeSum(Measurement::sumSunshineDurationHours),
                sumRainfallMillimeters = measurements.nullsafeSum(Measurement::sumRainfallMillimeters),
                sumSnowfallMillimeters = measurements.nullsafeSum(Measurement::sumSnowfallMillimeters),
                avgWindSpeedMetersPerSecond = measurements.nullsafeAvg(Measurement::avgWindSpeedMetersPerSecond),
                maxWindSpeedMetersPerSecond = measurements.nullsafeMax(Measurement::maxWindSpeedMetersPerSecond),

                )

        }

    private fun fileIsMeasurementFile(filename: String) =
        filename.startsWith("produkt_") && filename.endsWith(".txt")
}

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

