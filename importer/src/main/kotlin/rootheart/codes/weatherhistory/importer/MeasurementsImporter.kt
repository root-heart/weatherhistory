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
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementEntity
import rootheart.codes.weatherhistory.database.daily.groupDailyByMonth
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurementEntity
import rootheart.codes.weatherhistory.database.summarized.groupMonthlyByYear
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import java.util.zip.ZipInputStream
import kotlin.reflect.KMutableProperty0
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
    val measurementByTime = ConcurrentHashMap<LocalDate, DailyMeasurementEntity>()

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
            val monthlySummarized = groupDailyByMonth(measurementByTime.values)
            val yearlySummarized = groupMonthlyByYear(monthlySummarized)

            log.info { "Station ${station.id} - Inserting ${measurementByTime.size} detailed measurements into the database" }
            measurementByTime.values.chunked(1024).parallelStream().forEach(::insertDailyMeasurementsIntoDatabase)

            log.info { "Station ${station.id} - Inserting ${monthlySummarized.size + yearlySummarized.size} summarized measurements into the database" }
            monthlySummarized.map {
                SummarizedMeasurementEntity(
                        year = it.year,
                        month = it.month,
                        stationId = it.measurements.stationId,
                        airTemperatureCentigrade = it.measurements.airTemperatureCentigrade,
                        dewPointTemperatureCentigrade = it.measurements.dewPointTemperatureCentigrade,
                        humidityPercent = it.measurements.humidityPercent,
                        airPressureHectopascals = it.measurements.airPressureHectopascals,
                        windSpeedMetersPerSecond = it.measurements.windSpeedMetersPerSecond,
                        visibilityMeters = it.measurements.visibilityMeters,
                        sunshineMinutes = it.measurements.sunshineMinutes,
                        rainfallMillimeters = it.measurements.rainfallMillimeters,
                        snowfallMillimeters = it.measurements.snowfallMillimeters,
                        detailedCloudCoverage = it.measurements.detailedCloudCoverage,
                        cloudCoverageHistogram = it.measurements.cloudCoverageHistogram,
                        detailedWindDirectionDegrees = it.measurements.detailedWindDirectionDegrees,
                )
            }.let { insertSummarizedMeasurementsIntoDatabase(it) }

            yearlySummarized.map {
                SummarizedMeasurementEntity(
                        year = it.year,
                        month = null,
                        stationId = it.measurements.stationId,
                        airTemperatureCentigrade = it.measurements.airTemperatureCentigrade,
                        dewPointTemperatureCentigrade = it.measurements.dewPointTemperatureCentigrade,
                        humidityPercent = it.measurements.humidityPercent,
                        airPressureHectopascals = it.measurements.airPressureHectopascals,
                        windSpeedMetersPerSecond = it.measurements.windSpeedMetersPerSecond,
                        visibilityMeters = it.measurements.visibilityMeters,
                        sunshineMinutes = it.measurements.sunshineMinutes,
                        rainfallMillimeters = it.measurements.rainfallMillimeters,
                        snowfallMillimeters = it.measurements.snowfallMillimeters,
                        detailedCloudCoverage = it.measurements.detailedCloudCoverage,
                        cloudCoverageHistogram = it.measurements.cloudCoverageHistogram,
                        detailedWindDirectionDegrees = it.measurements.detailedWindDirectionDegrees,
                )
            }.let { insertSummarizedMeasurementsIntoDatabase(it) }

            log.info { "Station ${station.id} - Inserted measurements into the database" }
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
            val DailyMeasurementEntity = measurementByTime.getOrPut(day) {
                DailyMeasurementEntity(stationId = station.id!!, date = day)
            }
            when (measurementType) {
                MeasurementType.AIR_TEMPERATURE   -> setHourlyAirTemperatureData(DailyMeasurementEntity, hour, row)
                MeasurementType.CLOUD_COVERAGE    -> setHourlyCloudCoverageData(DailyMeasurementEntity, hour, row)
                MeasurementType.DEW_POINT         -> setHourlyDewPointData(DailyMeasurementEntity, hour, row)
                MeasurementType.MAX_WIND_SPEED    -> setHourlyMaxWindSpeedData(DailyMeasurementEntity, hour, row)
                MeasurementType.MOISTURE          -> setHourlyMoistureData(DailyMeasurementEntity, hour, row)
                MeasurementType.SUNSHINE_DURATION -> setHourlySunshineDurationData(DailyMeasurementEntity, hour, row)
                MeasurementType.VISIBILITY        -> setHourlyVisibilityData(DailyMeasurementEntity, hour, row)
                MeasurementType.WIND_SPEED        -> setHourlyWindSpeedData(DailyMeasurementEntity, hour, row)
                MeasurementType.PRECIPITATION     -> setHourlyPrecipitationData(row, DailyMeasurementEntity, hour)
                MeasurementType.DAILY             -> setDailyData(DailyMeasurementEntity, row)
            }
        }

        // TODO fix some data issues
        measurementByTime.values.forEach { m ->

            m.measurements.airPressureHectopascals.min = m.measurements.airPressureHectopascals.details?.nullsafeMin()
            m.measurements.airPressureHectopascals.max = m.measurements.airPressureHectopascals.details?.nullsafeMax()

            m.measurements.dewPointTemperatureCentigrade.min = m.measurements.dewPointTemperatureCentigrade.details?.nullsafeMin()
            m.measurements.dewPointTemperatureCentigrade.avg = m.measurements.dewPointTemperatureCentigrade.details?.nullsafeAvgDecimals()
            m.measurements.dewPointTemperatureCentigrade.max = m.measurements.dewPointTemperatureCentigrade.details?.nullsafeMax()

            m.measurements.visibilityMeters.min = m.measurements.visibilityMeters.details?.nullsafeMin()
            m.measurements.visibilityMeters.avg = m.measurements.visibilityMeters.details?.nullsafeAvgDecimals()
            m.measurements.visibilityMeters.max = m.measurements.visibilityMeters.details?.nullsafeMax()

            m.measurements.humidityPercent.min = m.measurements.humidityPercent.details?.nullsafeMin()
            m.measurements.humidityPercent.avg = m.measurements.humidityPercent.details?.nullsafeAvgDecimals()
            m.measurements.humidityPercent.max = m.measurements.humidityPercent.details?.nullsafeMax()

            m.measurements.sunshineMinutes.sum = m.measurements.sunshineMinutes.details?.filterNotNull()?.sumOf { it }

            val histogram = Array(10) { 0 }
            m.measurements.detailedCloudCoverage?.filterNotNull()
                    ?.map { if (it == -1) 9 else it }
                    ?.forEach { histogram[it]++ }
            m.measurements.cloudCoverageHistogram = histogram
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

//private fun summarize(measurements: Collection<DailyMeasurementEntity>): Collection<SummarizedMeasurementEntity> {
//    val summarizedByMonth = measurements
//            .groupBy { LocalDate(it.date.year, it.date.monthOfYear, 1) }
//            .mapValues { summarizeDaily(it.value) }
//    val summarizedByYear = summarizedByMonth.values
//            .groupBy { LocalDate(it.date.year, 1, 1) }
//            .mapValues { summarizeSummarized(it.value) }
//    return summarizedByMonth.values + summarizedByYear.values
//}
//
private fun setHourlyAirTemperatureData(
        measurementRecord: DailyMeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    setHourlyValue(measurementRecord.measurements.airTemperatureCentigrade::details, hour, row["TT_TU"]?.let(::BigDecimal))
    setHourlyValue(measurementRecord.measurements.humidityPercent::details, hour, row["RF_TU"]?.let(::BigDecimal))
}

private inline fun <reified N : Number> setHourlyValue(accessor: KMutableProperty0<Array<N?>?>, hour: Int, value: N?) {
    val details = accessor.get() ?: Array<N?>(24) { null }.also(accessor::set)
    details[hour] = value
}

private inline fun <reified N : Number> MinAvgMaxDetails<N>.setHourlyValue(hour: Int, value: N?) {
    if (details == null) {
        details = Array(24) { null }
    }
    details!![hour] = value
}

private inline fun <reified N : Number> AvgMaxDetails<N>.setHourlyValue(hour: Int, value: N?) {
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
        measurementRecord: DailyMeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    setHourlyValue(measurementRecord.measurements::detailedCloudCoverage, hour, row["V_N"]?.toInt())
}

private fun setHourlyDewPointData(measurementRecord: DailyMeasurementEntity, hour: Int,
                                  row: SemicolonSeparatedValues.Row) {
    setHourlyValue(measurementRecord.measurements.dewPointTemperatureCentigrade::details, hour, row["TD"]?.let(::BigDecimal))
}

private fun setHourlyMaxWindSpeedData(
        measurementRecord: DailyMeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    setHourlyValue(measurementRecord.measurements.windSpeedMetersPerSecond::details, hour, row["FX_911"]?.let(::BigDecimal))
}

private fun setHourlyMoistureData(measurementRecord: DailyMeasurementEntity, hour: Int,
                                  row: SemicolonSeparatedValues.Row) {
    setHourlyValue(measurementRecord.measurements.airPressureHectopascals::details, hour, row["P_STD"]?.let(::BigDecimal))
}

private fun setHourlySunshineDurationData(
        measurementRecord: DailyMeasurementEntity, hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    setHourlyValue(measurementRecord.measurements.sunshineMinutes::details, hour, row["SD_SO"]?.let(::BigDecimal))
}

private fun setHourlyVisibilityData(
        measurementRecord: DailyMeasurementEntity,
        hour: Int,
        row: SemicolonSeparatedValues.Row
) {
    setHourlyValue(measurementRecord.measurements.visibilityMeters::details, hour, row["V_VV"]?.let(::BigDecimal))
}

private fun setHourlyWindSpeedData(measurementRecord: DailyMeasurementEntity, hour: Int,
                                   row: SemicolonSeparatedValues.Row) {
    setHourlyValue(measurementRecord.measurements.windSpeedMetersPerSecond::details, hour, row["F"]?.let(::BigDecimal))
    measurementRecord.measurements.detailedWindDirectionDegrees?.set(hour, row["D"]?.toInt())
}

private fun setHourlyPrecipitationData(
        row: SemicolonSeparatedValues.Row,
        measurementRecord: DailyMeasurementEntity,
        hour: Int
) {
    val precipitationTypeCodeString = row["WRTR"]
    if (precipitationTypeCodeString == "6") {
        setHourlyValue(measurementRecord.measurements.rainfallMillimeters::details, hour, row["R1"]?.let(::BigDecimal))
    } else if (precipitationTypeCodeString == "7") {
        setHourlyValue(measurementRecord.measurements.snowfallMillimeters::details, hour, row["R1"]?.let(::BigDecimal))
    }
}

private fun setDailyData(measurementRecord: DailyMeasurementEntity, row: SemicolonSeparatedValues.Row) {
    measurementRecord.measurements.windSpeedMetersPerSecond.max = row["FX"]?.let(::BigDecimal)
    measurementRecord.measurements.windSpeedMetersPerSecond.avg = row["FM"]?.let(::BigDecimal)
    val precipitationType = row["RSKF"]
    if (precipitationType == "6") {
        measurementRecord.measurements.rainfallMillimeters.sum = row["RSK"]?.let(::BigDecimal)
    } else if (precipitationType == "7") {
        measurementRecord.measurements.snowfallMillimeters.sum = row["RSK"]?.let(::BigDecimal)
    }
    // measurementRecord.sumSunshineDurationMinutes = row["SDK"]?.let(::BigDecimal)?.multiply(sixty)?.intValueExact()
    // measurementRecord.snowheightCentimeters = row["SHK"]?.let(::BigDecimal)
    measurementRecord.measurements.airPressureHectopascals.avg = row["PM"]?.let(::BigDecimal)
    measurementRecord.measurements.airTemperatureCentigrade.avg = row["TMK"]?.let(::BigDecimal)
    measurementRecord.measurements.humidityPercent.avg = row["UPM"]?.let(::BigDecimal)
    measurementRecord.measurements.airTemperatureCentigrade.max = row["TXK"]?.let(::BigDecimal)
    measurementRecord.measurements.airTemperatureCentigrade.min = row["TNK"]?.let(::BigDecimal)
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

