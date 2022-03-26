package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.WeatherDb
import rootheart.codes.weatherhistory.importer.html.HtmlDirectory
import rootheart.codes.weatherhistory.importer.html.HtmlDirectoryParser
import java.math.BigDecimal
import java.net.URL
import java.nio.charset.Charset
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

    Database.connect(WeatherDb.dataSource)

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

@DelicateCoroutinesApi
private fun importMeasurements(rootDirectory: HtmlDirectory) {
    val stationIds = setOf("00848", "13776", "01993", "04371", "00662", "02014", "00850", "00691", "01443")
    val stationByExternalId = StationDao.findAll().associateBy(Station::externalId)
    val zippedDataFilesByExternalId = rootDirectory
        .getAllZippedDataFiles()
        .groupBy { it.externalId }
        .filter { stationIds.contains(it.key) }
        .mapKeys { stationByExternalId[it.key]!! }

    val duration = measureTimeMillis {
        runBlocking(Dispatchers.Default) {
            zippedDataFilesByExternalId.forEach { (station, zippedDataFiles) ->
                DataFileForStationImporter.import(this, station, zippedDataFiles)
            }
        }
    }
    log.info { "Finished import in $duration milliseconds, exiting program" }
}

private fun createStation(line: String) = Station(
    externalId = line.substring(0, 5),
    height = line.substring(24, 39).trim { it <= ' ' }.toInt(),
    latitude = BigDecimal(line.substring(41, 50).trim { it <= ' ' }),
    longitude = BigDecimal(line.substring(51, 60).trim { it <= ' ' }),
    name = line.substring(61, 102).trim { it <= ' ' },
    federalState = line.substring(102).trim { it <= ' ' })
