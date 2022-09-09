package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.common.ConcurrentExecutor
import rootheart.codes.common.strings.splitAndTrimTokens
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.HourlyMeasurementsImporter
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.SummarizedMeasurementImporter
import java.io.ByteArrayInputStream
import java.math.BigDecimal
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
private val downloadThreads = CoroutineScope(newFixedThreadPoolContext(2, "download"))

private val jobs = ArrayList<Job>()

@DelicateCoroutinesApi
fun importMeasurements(rootDirectory: HtmlDirectory) {
    val stationByExternalId = StationDao.findAll().associateBy(Station::externalId)
    val duration = measureTimeMillis {
        runBlocking {
            rootDirectory.getAllZippedDataFiles()
                .groupBy { it.externalId }
                .mapKeys { stationByExternalId[it.key] }
                .filter { it.key != null }
                .map { MeasurementsImporter(it.key!!, it.value) }
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
    val measurementByTime = ConcurrentHashMap<DateTime, HourlyMeasurement>()

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
            HourlyMeasurementsImporter.importEntities(measurementByTime.values)
            val summarizedMeasurements = summarizeMeasurements(measurementByTime.values)
            SummarizedMeasurementImporter.importEntities(summarizedMeasurements)
        }
    }

    private fun unzipAndConvert(zippedDataFile: ZippedDataFile, zippedBytes: ByteArray) {
        val durationAndRowCount = measureTimedValue {
            val unzippedBytes = unzip(zippedBytes)
            val parsed = parse(unzippedBytes)
            convert(parsed, zippedDataFile.measurementType.propertyByName)
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
        propertyByName: Map<String, MeasurementProperty<*>>
    ) {
        val indexMeasurementTime = semicolonSeparatedValues.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        val propertyByIndex = propertyByName.mapKeys {
            semicolonSeparatedValues.columnNames.indexOf(it.key)
        }
        for (row in semicolonSeparatedValues.rows) {
            val measurementTimeString = row[indexMeasurementTime]
            val measurementTime = DATE_TIME_FORMATTER.parseDateTime(measurementTimeString)
            val record = measurementByTime.getOrPut(measurementTime) {
                HourlyMeasurement(station = station, measurementTime = measurementTime)
            }
            for (indexAndProperty in propertyByIndex) {
                val stringValue = row[indexAndProperty.key]
                if (stringValue != null) {
                    indexAndProperty.value.setValue(record, stringValue)
                }
            }
        }

        // fix some data issues
        val list = measurementByTime.values.sortedBy { it.measurementTime }
        for ((index, measurement) in list.withIndex()) {
            if (measurement.precipitationMillimeters != null
                && measurement.precipitationMillimeters!! > BigDecimal.ZERO
                && measurement.precipitationType == null
            ) {
                if (index > 0) {
                    if (list[index - 1].precipitationType != null) {
                        measurement.precipitationType = list[index - 1].precipitationType
                    } else if (index < list.size - 1) {
                        measurement.precipitationType = list[index + 1].precipitationType
                    }
                }
            }
        }
    }

    private fun summarizeMeasurements(measurements: Collection<HourlyMeasurement>): List<SummarizedMeasurement> {
        val summarizedMeasurements = ArrayList<SummarizedMeasurement>()
        val duration = measureTimeMillis {
            val summarizedByDay = Summarizer.summarize(station, DateInterval::day, measurements)
            summarizedMeasurements += summarizedByDay

            runBlocking {
                launch {
                    val groupedByMonth = summarizedByDay.groupBy { DateInterval.month(it.firstDay) }
                    val summarizedByMonth = groupedByMonth.map { (month, measurements) ->
                        Summarizer.summarizeSummarizedRecords(station, month, measurements)
                    }

                    val groupedByYear = summarizedByMonth.groupBy { DateInterval.year(it.firstDay) }
                    val summarizedByYear = groupedByYear.map { (year, measurements) ->
                        Summarizer.summarizeSummarizedRecords(station, year, measurements)
                    }

                    val groupedByDecade = summarizedByYear.groupBy { DateInterval.decade(it.firstDay) }
                    val summarizedByDecade = groupedByDecade.map { (decade, measurements) ->
                        Summarizer.summarizeSummarizedRecords(station, decade, measurements)
                    }

                    synchronized(summarizedMeasurements) {
                        summarizedMeasurements += summarizedByMonth
                        summarizedMeasurements += summarizedByYear
                        summarizedMeasurements += summarizedByDecade
                    }
                }

                launch {
                    val groupedBySeason = summarizedByDay.groupBy { DateInterval.season(it.firstDay) }
                    val summarizedBySeason = groupedBySeason.map { (season, measurements) ->
                        Summarizer.summarizeSummarizedRecords(station, season, measurements)
                    }
                    synchronized(summarizedMeasurements) {
                        summarizedMeasurements += summarizedBySeason
                    }
                }
            }
        }
        log.info { "Summarizing measurements done in $duration millis" }
        return summarizedMeasurements
    }

    private fun fileIsMeasurementFile(filename: String) =
        filename.startsWith("produkt_") && filename.endsWith(".txt")
}

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC()
private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"

