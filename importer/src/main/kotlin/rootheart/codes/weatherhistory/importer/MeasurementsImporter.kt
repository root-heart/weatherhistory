package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.common.strings.splitAndTrimTokens
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.HourlyMeasurementsImporter
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.SummarizedMeasurementImporter
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import java.util.zip.ZipInputStream
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

@DelicateCoroutinesApi
private val unzipContext = newFixedThreadPoolContext(40, "unzip")

@DelicateCoroutinesApi
fun importMeasurements(rootDirectory: HtmlDirectory) {
    val stationIds = setOf(/*"00848", "13776", "01993", "04371", "00662", "02014", "00850", "01443",*/ "00691")
    val stationByExternalId = StationDao.findAll().associateBy(Station::externalId)
    val duration = measureTimeMillis {
        runBlocking {
            rootDirectory.getAllZippedDataFiles()
                .filter { stationIds.contains(it.externalId) }
                .groupBy { it.externalId }
                .mapKeys { stationByExternalId[it.key]!! }
                .forEach(::convertAndImport)
        }
    }
    log.info { "Finished import in $duration milliseconds, exiting program" }
}

@DelicateCoroutinesApi
private fun CoroutineScope.convertAndImport(station: Station, zippedDataFiles: List<ZippedDataFile>) {
    val xyz = Xyz(station)
    val measurements = xyz.downloadAndConvert(zippedDataFiles)
    log.info { "${station.id} - Converted: ${measurements.size}" }

    launch { HourlyMeasurementsImporter.importEntities(measurements) }

    launch {
        val summarizedMeasurements = xyz.summarizeMeasurements(measurements)
        SummarizedMeasurementImporter.importEntities(summarizedMeasurements)
    }

    log.info { "Station ${station.id} - Converted and saved: ${measurements.size}" }
}

@DelicateCoroutinesApi
private class Xyz(val station: Station) {
    private val measurementByTime = ConcurrentHashMap<DateTime, HourlyMeasurement>()

    @DelicateCoroutinesApi
    fun downloadAndConvert(zippedDataFiles: Collection<ZippedDataFile>): Collection<HourlyMeasurement> =
        runBlocking {
            for (zippedDataFile in zippedDataFiles.sortedByDescending { it.size }) {
                log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - Downloading" }
                val zippedBytes = zippedDataFile.url.readBytes()
                log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - Downloaded" }

                unzipAndConvert(zippedDataFile, zippedBytes)
            }
            log.info { "Station ${station.id} - Waiting for unzip-parse-converters to finish their job" }
            return@runBlocking measurementByTime.values
        }

    private fun CoroutineScope.unzipAndConvert(zippedDataFile: ZippedDataFile, zippedBytes: ByteArray) =
        launch(unzipContext) {
            val duration = measureTimeMillis {
                unzip(zippedBytes)
                    .let(::parse)
                    .let { convert(it, zippedDataFile.measurementType.columnNameMapping) }
            }
            log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - converting took $duration millis" }
        }

    fun parse(bytes: ByteArray): SemicolonSeparatedValues =
        ByteArrayInputStream(bytes).bufferedReader().use { reader ->
            val header = reader.readLine() ?: ""
            val columnNames = splitAndTrimTokens(header).map { it!! }
            val columnValues = reader.lines().map(::splitAndTrimTokens).collect(Collectors.toList())
            return SemicolonSeparatedValues(columnNames, columnValues)
        }

    private fun convert(semicolonSeparatedValues: SemicolonSeparatedValues, columnNameMapping: Map<String, SimpleMeasurementProperty<*>>) {
        val indexMeasurementTime =
            semicolonSeparatedValues.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        val columnMappingByIndex = columnNameMapping.mapKeys {
            semicolonSeparatedValues.columnNames.indexOf(it.key)
        }
        for (row in semicolonSeparatedValues.rows) {
            val measurementTimeString = row[indexMeasurementTime]
            val measurementTime = DATE_TIME_FORMATTER.parseDateTime(measurementTimeString)
            val record = measurementByTime.getOrPut(measurementTime) {
                HourlyMeasurement(station = station, measurementTime = measurementTime)
            }
            for (columnIndex in columnMappingByIndex) {
                val stringValue = row[columnIndex.key]
                if (stringValue != null) {
                    columnIndex.value.setValue(record, stringValue)
                }
            }
        }
    }

    @DelicateCoroutinesApi
    fun summarizeMeasurements(measurements: Collection<HourlyMeasurement>): List<SummarizedMeasurement> {
        val summarizedMeasurements = ArrayList<SummarizedMeasurement>()
        val duration = measureTimeMillis {
            val groupedByDay = measurements.groupBy { DateInterval.day(it.measurementTime) }
            val summarizedByDay = groupedByDay.map { (day, measurements) ->
                Summarizer.summarizeHourlyRecords(station, day, measurements)
            }

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

            summarizedMeasurements += summarizedByDay
            summarizedMeasurements += summarizedByMonth
            summarizedMeasurements += summarizedByYear
            summarizedMeasurements += summarizedByDecade
        }
        log.info { "Summarizing measurements done in $duration millis" }
        return summarizedMeasurements
    }

    private fun unzip(zippedBytes: ByteArray): ByteArray = ZipInputStream(ByteArrayInputStream(zippedBytes))
        .use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsMeasurementFile(it.name) }) {
                return@use zipInputStream.readBytes()
            } else {
                return@use ByteArray(0)
            }
        }

    private fun fileIsMeasurementFile(filename: String) =
        filename.startsWith("produkt_") && filename.endsWith(".txt")
}

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC()
private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"

