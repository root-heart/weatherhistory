package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.importer.html.HtmlDirectoryParser
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.system.exitProcess

private val log = KotlinLogging.logger {}

fun main(args: Array<String>) {
    var baseUrlString =
        if (args.size == 1) args[0]
        else "https://opendata.dwd.de/climate_environment/CDC/observations_germany/climate/hourly/"
    if (!baseUrlString.endsWith("/")) {
        baseUrlString += "/"
    }
    val baseUrl = URL(baseUrlString)
    val rootDirectory = HtmlDirectoryParser.parseHtml(baseUrl)
    val stationsFiles = rootDirectory.getAllStationsFiles()
    val stations = HashMap<StationId, Station>()
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
                stations[station.stationId] = station
            }
        }
    }

    StationsImporter.importEntities(stations.values)
//    exitProcess(0)


    val zippedDataFiles = rootDirectory.getAllZippedDataFiles()
    val threadPool = Executors.newFixedThreadPool(20)
    val futures = ArrayList<Future<*>>()
    zippedDataFiles.groupBy { it.stationId }.filter { it.key.stationId == 691 }.forEach {
        futures += threadPool.submit { DataFileForStationImporter.import(it.value) }
    }
    futures.forEach { it.get() }
    log.info { "Finished import, exiting program" }
    exitProcess(0)
}

private fun createStation(line: String) = Station(
    stationId = StationId.of(line.substring(0, 5)),
    height = line.substring(24, 39).trim { it <= ' ' }.toInt(),
    latitude = BigDecimal(line.substring(41, 50).trim { it <= ' ' }),
    longitude = BigDecimal(line.substring(51, 60).trim { it <= ' ' }),
    name = line.substring(61, 102).trim { it <= ' ' },
    federalState = line.substring(102).trim { it <= ' ' })

