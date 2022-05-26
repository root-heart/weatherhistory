package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.DateTime
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.HourlyMeasurementsImporter
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.StationsImporter
import rootheart.codes.weatherhistory.database.SummarizedMeasurementImporter
import rootheart.codes.weatherhistory.database.WeatherDb
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

@DelicateCoroutinesApi
fun main(args: Array<String>) {
    var baseUrlString =
        if (args.size == 1) args[0]
        else "https://opendata.dwd.de/climate_environment/CDC/observations_germany/climate/hourly/"
    if (!baseUrlString.endsWith("/")) {
        baseUrlString += "/"
    }
    val baseUrl = URL(baseUrlString)
    val rootDirectory = HtmlDirectoryParser.parseHtml(baseUrl)

    WeatherDb.connect()

    importStations(rootDirectory)
    importMeasurements(rootDirectory)

    exitProcess(0)
}

@DelicateCoroutinesApi
private fun importStations(rootDirectory: HtmlDirectory) {
    val stationsFiles = rootDirectory.getAllStationsFiles()
    val stations = HashMap<String, Station>()
    stationsFiles.forEach {
        log.info { "Parsing stations from ${it.url}" }
        val lines = it.url.readText(Charset.forName("Cp1252")).lines()
        if (lines[0] != "Stations_id von_datum bis_datum Stationshoehe geoBreite geoLaenge Stationsname Bundesland" ||
            lines[1] != "----------- --------- --------- ------------- --------- --------- ----------------------------------------- ----------"
        ) {
            throw UnsupportedOperationException()
        }
        for (i in 2 until lines.size) {
            if (lines[i].trim().isNotEmpty()) {
                val station = createStation(lines[i])
                stations[station.externalId] = station
            }
        }
    }

    StationsImporter.importEntities(stations.values)
}

private val unzipContext = newFixedThreadPoolContext(40, "unzip")

@DelicateCoroutinesApi
private fun importMeasurements(rootDirectory: HtmlDirectory) {
//    val stationIds = setOf("00848", "13776", "01993", "04371", "00662", "02014", "00850", "00691", "01443")
    val stationByExternalId = StationDao.findAll().associateBy(Station::externalId)
    val zippedDataFilesByExternalId = rootDirectory
        .getAllZippedDataFiles()
        .groupBy { it.externalId }
//        .filter { stationIds.contains(it.key) }
        .mapKeys { stationByExternalId[it.key]!! }

    val duration = measureTimeMillis {
        runBlocking(Dispatchers.Default) {
            zippedDataFilesByExternalId.forEach { (station, zippedDataFiles) ->
                val measurements = downloadAndConvert(station, zippedDataFiles)
                log.info { "${station.id} - Converted: ${measurements.size}" }

                launch {
                    log.info { "Import hourly measurements" }
                    HourlyMeasurementsImporter.importEntities(measurements)
                    log.info { "Import hourly measurements done" }
                }

                launch {
                    log.info { "Summarize hourly measurements" }
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
                    log.info { "Summarize hourly measurements done, summing" }

                    val summarizedMeasurements =
                        summarizedByDay + summarizedByMonth + summarizedByYear + summarizedByDecade
                    log.info { "Summing summarized measurements done" }

                    log.info { "Importing summarized measurements" }
                    SummarizedMeasurementImporter.importEntities(summarizedMeasurements)
                    log.info { "Importing summarized measurements done" }
                }
                log.info { "Station ${station.id} - Converted and saved: ${measurements.size}" }
            }
        }
    }
    log.info { "Finished import in $duration milliseconds, exiting program" }
}

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
                log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - Unzipping ${zippedBytes.size} bytes" }
                val unzippedContent = ZipInputStream(ByteArrayInputStream(zippedBytes))
                    .use { zipInputStream ->
                        val entries = generateSequence { zipInputStream.nextEntry }
                        if (entries.any { fileIsMeasurementFile(it.name) }) {
                            return@use zipInputStream.readBytes()
                        } else {
                            return@use null
                        }
                    }
                log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - Unzipped to ${unzippedContent?.size} bytes" }
                val inputStream =
                    unzippedContent?.let(::ByteArrayInputStream) ?: InputStream.nullInputStream()
                val semicolonSeparatedValues =
                    inputStream.bufferedReader().use(SemicolonSeparatedValuesParser::parse)
                log.info { "Station ${station.id}, file ${zippedDataFile.fileName} - Parsed to ${semicolonSeparatedValues.rows.size} rows" }
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
        }
    log.info { "Station ${station.id} - Waiting for unzip-parse-converters to finish their job" }
    return@runBlocking measurementByTime.values
}

private fun createStation(line: String) = Station(
    externalId = line.substring(0, 5),
    height = line.substring(24, 39).trim { it <= ' ' }.toInt(),
    latitude = BigDecimal(line.substring(41, 50).trim { it <= ' ' }),
    longitude = BigDecimal(line.substring(51, 60).trim { it <= ' ' }),
    name = line.substring(61, 102).trim { it <= ' ' },
    federalState = line.substring(102).trim { it <= ' ' })
