package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.DAO
import rootheart.codes.weatherhistory.database.HistogramDao
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.MinAvgMaxDao
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SumDao
import rootheart.codes.weatherhistory.restapp.optPathParam
import rootheart.codes.weatherhistory.restapp.optQueryParam
import rootheart.codes.weatherhistory.restapp.requiredPathParam

fun Routing.stationsResource() {
    route("stations") {
        get { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

        route("{stationId}") {
            get {
                val stationId = requiredPathParam("stationId") { it.toLong() }
                call.respond(StationDao.findById(stationId) ?: HttpStatusCode.NotFound)
            }

            get("{measurementType}/{year}/{month?}/{day?}") {
                val stationId = requiredPathParam("stationId") { it.toLong() }
                val dao = requiredPathParam("measurementType") { measurementTypeColumnsMapping[it] }
                val year = requiredPathParam("year") { it.toInt() }
                val month = optPathParam("month") { it.toInt() }
                val day = optPathParam("day") { it.toInt() }
                val firstDay = LocalDate(year, month ?: 1, day ?: 1)
                val lastDay =
                    if (month == null) firstDay.plusYears(1)
                    else if (day == null) firstDay.plusMonths(1)
                    else firstDay.plusDays(1)
                val resolution = optQueryParam("resolution") { requestResolutionToIntervalMapping[it] }
                    ?: if (month == null) Interval.MONTH
                    else Interval.DAY
                val data = dao.findAll(stationId, firstDay, lastDay, resolution)
                call.respond(data)
            }
        }
    }
}

val measurementTypeColumnsMapping: Map<String, DAO<*, out Number?>> = with(MeasurementsTable) {
    mapOf(
        "temperature" to MinAvgMaxDao(
            minAirTemperatureCentigrade,
            avgAirTemperatureCentigrade,
            maxAirTemperatureCentigrade,
            detailedAirTemperatureCentigrade,
        ),
        "air-pressure" to MinAvgMaxDao(
            minAirPressureHectopascals,
            avgAirPressureHectopascals,
            maxAirPressureHectopascals,
            detailedAirPressureHectopascals,
        ),
        "dew-point-temperature" to MinAvgMaxDao(
            minDewPointTemperatureCentigrade,
            avgDewPointTemperatureCentigrade,
            maxDewPointTemperatureCentigrade,
            detailedAirPressureHectopascals
        ),
        "humidity" to MinAvgMaxDao(minHumidityPercent, avgHumidityPercent, maxHumidityPercent, detailedHumidityPercent),
        "visibility" to MinAvgMaxDao(
            minVisibilityMeters,
            avgVisibilityMeters,
            maxVisibilityMeters,
            detailedVisibilityMeters
        ),
        "wind-speed" to MinAvgMaxDao(
            null,
            avgWindSpeedMetersPerSecond,
            maxWindSpeedMetersPerSecond,
            detailedWindSpeedMetersPerSecond
        ),
        "sunshine-duration" to SumDao(sumSunshineDurationHours),
        "precipitation" to SumDao(sumRainfallMillimeters, sumSnowfallMillimeters),
        "cloud-coverage" to HistogramDao(cloudCoverageHistogram)
    )
}

private val requestResolutionToIntervalMapping = mapOf(
    "daily" to Interval.DAY,
    "monthly" to Interval.MONTH,
    "yearly" to Interval.YEAR
)
