package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import mu.KotlinLogging
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationsImporter
import java.math.BigDecimal
import java.nio.charset.Charset

private val log = KotlinLogging.logger {}

@DelicateCoroutinesApi
fun importStations(rootDirectory: HtmlDirectory) {
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

private fun createStation(line: String) = Station(
    externalId = line.substring(0, 5),
    height = line.substring(24, 39).trim { it <= ' ' }.toInt(),
    latitude = BigDecimal(line.substring(41, 50).trim { it <= ' ' }),
    longitude = BigDecimal(line.substring(51, 60).trim { it <= ' ' }),
    name = line.substring(61, 102).trim { it <= ' ' },
    federalState = line.substring(102).trim { it <= ' ' })
