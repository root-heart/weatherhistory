package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.DAO
import rootheart.codes.weatherhistory.database.HistogramDao
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.MinAvgMaxDao
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SumDao

fun Routing.stationsResource() {
    route("stations") {
        get { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

        route("{stationId}") {
            get {
                val stationId = require("stationId") { it.toLong() }
                call.respond(StationDao.findById(stationId) ?: HttpStatusCode.NotFound)
            }

            get("{measurementType}/{year}/{month?}/{day?}") {
                val stationId = require("stationId") { it.toLong() }
                val dao = require("measurementType") { measurementTypeColumnsMapping[it] }
                val year = require("year") { it.toInt() }
                val month = opt("month") { it.toInt() }
                val day = opt("day") { it.toInt() }
                val firstDay = LocalDate(year, month ?: 1, day ?: 1)
                val lastDay =
                    if (month == null) firstDay.plusYears(1)
                    else if (day == null) firstDay.plusMonths(1)
                    else firstDay.plusDays(1)
                val resolution = query("resolution") { requestResolutionToIntervalMapping[it] }
                    ?: if (month == null) Interval.MONTH
                    else Interval.DAY
                val data = dao.findAll(stationId, firstDay, lastDay, resolution)
                call.respond(data)
            }
        }
    }
}

private fun badRequest(message: String): Nothing = throw throw BadRequestException(message)

private fun <T> PipelineContext<Unit, ApplicationCall>.require(name: String, map: (String) -> T?): T =
    opt(name, map) ?: badRequest("value for parameter $name cannot be mapped")

private fun <T> tryMapOrBadRequest(value: String, map: (String) -> T): T? =
    try {
        map(value)
    } catch (e: Exception) {
        badRequest("value can not be mapped")
    }

private fun <T> PipelineContext<Unit, ApplicationCall>.opt(name: String, map: (String) -> T?): T? =
    call.parameters[name]?.let { tryMapOrBadRequest(it, map) }

private fun <T> PipelineContext<Unit, ApplicationCall>.query(name: String, map: (String?) -> T?): T? =
    map(call.request.queryParameters[name])


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
