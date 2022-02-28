package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.WeatherDb
import rootheart.codes.weatherhistory.importer.html.HtmlDirectory
import rootheart.codes.weatherhistory.importer.html.HtmlDirectoryParser
import rootheart.codes.weatherhistory.importer.html.ZippedDataFile
import rootheart.codes.weatherhistory.importer.ssv.SemicolonSeparatedValues
import rootheart.codes.weatherhistory.importer.ssv.SemicolonSeparatedValuesParser
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
private val importThreadPool = newFixedThreadPoolContext(8, "importer")

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

//    importStations(rootDirectory)
    Database.connect(WeatherDb.dataSource)

    val stationByExternalId = StationDao.findAll().associateBy(Station::externalId)
    val zippedDataFilesByExternalId = rootDirectory
        .getAllZippedDataFiles()
        .groupBy { it.externalId }
        .mapKeys { stationByExternalId[it.key]!! }
        .filter { it.key.externalId.toInt() == 691 }

    val duration = measureTimeMillis {
        runBlocking(Dispatchers.Default) {
            zippedDataFilesByExternalId.forEach { (station, zippedDataFiles) ->
                val downloaded = Channel<Pair<ZippedDataFile, ByteArray>>(zippedDataFiles.size)
                launchUnzipper(station, downloaded)
                download(zippedDataFiles, station, downloaded)
                log.info { "Station ${station.id} - Downloaded from ${zippedDataFiles.size} files" }
            }
        }
    }
    log.info { "Finished import in $duration milliseconds, exiting program" }
    exitProcess(0)
}

@DelicateCoroutinesApi
private fun CoroutineScope.launchUnzipper(
    station: Station,
    downloaded: Channel<Pair<ZippedDataFile, ByteArray>>
) = launch(CoroutineName("process-zipped-data-file")) {
    val measurements = abc(downloaded, station)
//                        HourlyMeasurementsImporter.importEntities(measurementByTime.values)

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

    val summarizedMeasurements =
        summarizedByDay + summarizedByMonth + summarizedByYear + summarizedByDecade;

    log.info { "Station ${station.id} - Converted: ${measurements.size}" }
}

private suspend fun download(
    zippedDataFiles: Collection<ZippedDataFile>,
    station: Station,
    downloaded: Channel<Pair<ZippedDataFile, ByteArray>>
) {
    zippedDataFiles.forEach { zippedDataFile ->
        log.info { "Station ${station.id} - Downloading from ${zippedDataFile.url}" }
        val content = zippedDataFile.url.readBytes()
        log.info { "Station ${station.id} - Downloaded from ${zippedDataFile.url}, ${content.size} bytes" }
        downloaded.send(Pair(zippedDataFile, content))
    }
    downloaded.close()
}

private suspend fun abc(
    downloaded: Channel<Pair<ZippedDataFile, ByteArray>>,
    station: Station
): Collection<HourlyMeasurement> {
    val measurementByTime = ConcurrentHashMap<DateTime, HourlyMeasurement>()
    coroutineScope {
        for (x in downloaded) {
            launch(CoroutineName("convert-zipped-data-file")) {
                currentCoroutineContext().job
                val zippedDataFile = x.first
                val content = x.second
                val unzippedContent = unzip(station, zippedDataFile, content)
                val semicolonSeparatedValues = parse(station, zippedDataFile, unzippedContent)
                convert(station, zippedDataFile, semicolonSeparatedValues, measurementByTime)
            }
        }
    }
    return measurementByTime.values
}

@DelicateCoroutinesApi
class SingleThreadedDownloader {
    private val singleThreadedContext = newSingleThreadContext("download")
    suspend fun download(url: URL): ByteArray {
        var content: ByteArray? = null
        coroutineScope {
            val job = launch(singleThreadedContext) {
                content = url.readBytes()
            }
            job.join()
        }
        return content!!
    }
}

private fun unzip(station: Station, zippedDataFile: ZippedDataFile, zippedBytes: ByteArray): ByteArray? {
    log.info { "Station ${station.id} - Unzipping ${zippedDataFile.url}" }
    val unzippedContent = ZipInputStream(ByteArrayInputStream(zippedBytes))
        .use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsMeasurementFile(it.name) }) {
                return@use zipInputStream.readBytes()
            } else {
                return@use null
            }
        }
    log.info { "Station ${station.id} - Unzipped ${zippedDataFile.url}, ${unzippedContent!!.size} bytes" }
    return unzippedContent
}

private fun parse(
    station: Station,
    zippedDataFile: ZippedDataFile,
    unzippedContent: ByteArray?
): SemicolonSeparatedValues {
    log.info { "Station ${station.id} - Parsing ${zippedDataFile.url}" }
    val inputStream = unzippedContent?.let(::ByteArrayInputStream) ?: InputStream.nullInputStream()
    val semicolonSeparatedValues = inputStream.bufferedReader().use(SemicolonSeparatedValuesParser::parse)
    log.info { "Station ${station.id} - Parsed ${zippedDataFile.url}, ${semicolonSeparatedValues.rows.size} rows" }
    return semicolonSeparatedValues
}


private fun convert(
    station: Station, zippedDataFile: ZippedDataFile, semicolonSeparatedValues: SemicolonSeparatedValues,
    measurementByTime: MutableMap<DateTime, HourlyMeasurement>
) {
    log.info { "Station ${station.id} - Converting ${zippedDataFile.url}" }
    val indexMeasurementTime = semicolonSeparatedValues.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
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
    log.info { "Station ${station.id} - Converted ${zippedDataFile.url}" }
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

private fun fileIsMeasurementFile(filename: String) = filename.startsWith("produkt_") && filename.endsWith(".txt")

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC()
private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"

private fun createStation(line: String) = Station(
    externalId = line.substring(0, 5),
    height = line.substring(24, 39).trim { it <= ' ' }.toInt(),
    latitude = BigDecimal(line.substring(41, 50).trim { it <= ' ' }),
    longitude = BigDecimal(line.substring(51, 60).trim { it <= ' ' }),
    name = line.substring(61, 102).trim { it <= ' ' },
    federalState = line.substring(102).trim { it <= ' ' })

class X(val zippedDataFile: ZippedDataFile, val content: ByteArray)

private suspend fun downloadAndSendToChannel(zippedDataFiles: Collection<ZippedDataFile>, channel: Channel<X>) {
    zippedDataFiles.forEach { zippedDataFile ->
        log.info { "Downloading from ${zippedDataFile.url}" }
        val content = zippedDataFile.url.readBytes()
        log.info { "Downloading ${content.size} bytes from ${zippedDataFile.url} finished" }
        channel.send(X(zippedDataFile, content))
        log.info { "Sent ${content.size} bytes from ${zippedDataFile.url} to processor" }
    }
}

//@DelicateCoroutinesApi
//private fun CoroutineScope.startParsingAndConverting(
//    channel: Channel<X>, station: Station, measurementByTime: MutableMap<DateTime, HourlyMeasurement>
//) = launch {
//    for (x in channel) {
//        log.info { "Received ${x.content.size} bytes to process downloaded from ${x.zippedDataFile.url}" }
//        launch {
//            log.info { "Unzipping data downloaded from ${x.zippedDataFile.url}" }
//            ZipInputStream(ByteArrayInputStream(x.content)).use { zipInputStream ->
//                val entries = generateSequence { zipInputStream.nextEntry }
//                if (entries.any { fileIsMeasurementFile(it.name) }) {
//                    val unzippedContent = zipInputStream.readBytes()
//                    log.info { "Unzipped data file from ${x.zippedDataFile.url}, ${unzippedContent.size} bytes. Parsing..." }
//                    val semicolonSeparatedValues = ByteArrayInputStream(unzippedContent)
//                        .bufferedReader()
//                        .use(SemicolonSeparatedValuesParser::parse)
//                    log.info { "Parsed ${semicolonSeparatedValues.rows.size} rows from ${x.zippedDataFile.url}. Converting..." }
//                    val measurementType = x.zippedDataFile.measurementType
//                    convert(station, semicolonSeparatedValues, measurementType, measurementByTime)
//                }
//            }
//            log.info { "Unzipping data downloaded from ${x.zippedDataFile.url} finished" }
//        }
//    }
//}
