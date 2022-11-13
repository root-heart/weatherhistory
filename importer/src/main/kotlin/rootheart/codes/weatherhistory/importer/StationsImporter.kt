package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import mu.KotlinLogging
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationsImporter
import java.math.BigDecimal
import java.nio.charset.Charset

private val log = KotlinLogging.logger {}

private val stationFilter: (Station) -> Boolean = {
    it.externalId == "00691"
//    it.hasRecentData && it.hasTemperatureData && it.hasSunshineData && it.hasCloudinessData && it.hasAirPressureData && it.hasWindData
}

@DelicateCoroutinesApi
fun importStations(stationsFiles: Collection<StationsFile>) {
    val stations = HashMap<String, Station>()
    stationsFiles.forEach { stationsFile ->
        log.info { "Parsing stations from ${stationsFile.url}" }
        val lines = stationsFile.url.readText(Charset.forName("Cp1252")).lines()
        if (lines[0] != "Stations_id von_datum bis_datum Stationshoehe geoBreite geoLaenge Stationsname Bundesland" ||
            lines[1] != "----------- --------- --------- ------------- --------- --------- ----------------------------------------- ----------"
        ) {
            throw UnsupportedOperationException()
        }
        for (i in 2 until lines.size) {
            if (lines[i].trim().isNotEmpty()) {
                val line = lines[i]
                val externalId = line.substring(0, 5)
                val lastDay = line.substring(15, 23)
                val station = stations.getOrPut(externalId) {
                    Station(
                        externalSystem = "DWD",
                        externalId = externalId,
                        height = line.substring(24, 39).trim { it <= ' ' }.toInt(),
                        latitude = BigDecimal(line.substring(41, 50).trim { it <= ' ' }),
                        longitude = BigDecimal(line.substring(51, 60).trim { it <= ' ' }),
                        name = line.substring(61, 102).trim { it <= ' ' },
                        federalState = line.substring(102).trim { it <= ' ' })
                }
                when (stationsFile.measurementType) {
                    MeasurementType.CLOUDINESS -> station.hasCloudinessData = true
                    MeasurementType.AIR_TEMPERATURE -> station.hasTemperatureData = true
                    MeasurementType.SUNSHINE_DURATION -> station.hasSunshineData = true
                    MeasurementType.DEW_POINT -> station.hasTemperatureData = true
                    MeasurementType.MAX_WIND_SPEED -> station.hasWindData = true
                    MeasurementType.MOISTURE -> station.hasAirPressureData = true
                    MeasurementType.VISIBILITY -> station.hasVisibilityData = true
                    MeasurementType.WIND_SPEED -> station.hasWindData = true
                    MeasurementType.PRECIPITATION -> station.hasPrecipitationData = true
                }
                station.hasRecentData = lastDay.startsWith(LocalDate.now().toString("yyyyMM"))
            }
        }
    }

    val filteredStations = stations.values.filter(stationFilter)
    StationsImporter.importEntities(filteredStations)
}

