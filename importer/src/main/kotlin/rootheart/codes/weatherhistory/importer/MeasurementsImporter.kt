package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.HourlyMeasurementsImporter
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.SummarizedMeasurementImporter
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

@DelicateCoroutinesApi
private val unzipContext = newFixedThreadPoolContext(40, "unzip")

@DelicateCoroutinesApi
fun importMeasurements(rootDirectory: HtmlDirectory) {
    val stationIds = setOf(/*"00848", "13776", "01993", "04371", "00662", "02014", "00850", "01443",*/ "00691")
    val stationByExternalId = StationDao.findAll().associateBy(Station::externalId)
    val zippedDataFilesByExternalId = rootDirectory
        .getAllZippedDataFiles()
        .groupBy { it.externalId }
        .filter { stationIds.contains(it.key) }
        .mapKeys { stationByExternalId[it.key]!! }

    val duration = measureTimeMillis {
        runBlocking(Dispatchers.Default) {
            zippedDataFilesByExternalId.forEach { (station, zippedDataFiles) ->
                val measurements = downloadAndConvert(station, zippedDataFiles)
                log.info { "${station.id} - Converted: ${measurements.size}" }

                launch { HourlyMeasurementsImporter.importEntities(measurements) }

                launch {
                    val summarizedMeasurements = summarizeMeasurements(measurements, station)
                    SummarizedMeasurementImporter.importEntities(summarizedMeasurements)
                }

                log.info { "Station ${station.id} - Converted and saved: ${measurements.size}" }
            }
        }
    }
    log.info { "Finished import in $duration milliseconds, exiting program" }
}

@DelicateCoroutinesApi
private fun downloadAndConvert(
    station: Station,
    zippedDataFiles: Collection<ZippedDataFile>
): Collection<HourlyMeasurement> = runBlocking {
    val measurementByTime = ConcurrentHashMap<DateTime, HourlyMeasurement>()
    zippedDataFiles
        .sortedByDescending { it.size }
        .forEach { zippedDataFile ->
            log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - Downloading" }
            val zippedBytes = zippedDataFile.url.readBytes()
            log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - Downloaded" }

            launch(unzipContext) {
                val duration = measureTimeMillis {
                    log.debug { "Station ${station.id}, file ${zippedDataFile.fileName} - Unzipping ${zippedBytes.size} bytes" }
                    val unzippedBytes = unzip(zippedBytes)
                    log.debug { "Station ${station.id}, file ${zippedDataFile.fileName} - Unzipped to ${unzippedBytes.size} bytes" }
                    val semicolonSeparatedValues = SemicolonSeparatedValuesParser.parse(unzippedBytes)
                    log.debug { "Station ${station.id}, file ${zippedDataFile.fileName} - Parsed to ${semicolonSeparatedValues.rows.size} rows" }
                    val indexMeasurementTime =
                        semicolonSeparatedValues.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
                    val columnMappingByIndex = zippedDataFile.measurementType.columnNameMapping.mapKeys {
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
                log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - converting took $duration millis" }
            }
        }
    log.info { "Station ${station.id} - Waiting for unzip-parse-converters to finish their job" }
    return@runBlocking measurementByTime.values
}

@DelicateCoroutinesApi
private fun summarizeMeasurements(
    measurements: Collection<HourlyMeasurement>,
    station: Station
): List<SummarizedMeasurement> {
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

private fun fileIsMeasurementFile(filename: String) = filename.startsWith("produkt_") && filename.endsWith(".txt")

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC()
private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"

