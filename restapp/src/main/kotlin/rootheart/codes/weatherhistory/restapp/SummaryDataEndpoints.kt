package rootheart.codes.weatherhistory.restapp

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.queryString
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateIntervalType
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.HourlyMeasurementDao
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.SummarizedMeasurementDao

private val log = KotlinLogging.logger { }

fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getSummary()
}

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC()

fun Route.getSummary() = get() {
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.request.queryParameters["year"]!!.toInt()
    log.info { "Fetching data for station id $stationId and year $year from database" }

    val station = StationDao.findById(stationId)!!

    val start = DateTime(year, 1, 1, 0, 0)
    val end = DateTime(year + 1, 1, 1, 0, 0)

    val summaryData = SummarizedMeasurementDao
        .findByStationIdAndDateBetween(station, start, end, DateIntervalType.DAY)
        .groupBy { it.firstDay.toLocalDate() }

    val measurements = HourlyMeasurementDao
        .findByStationIdAndYear(station, year)
        .groupBy { it.measurementTime.toLocalDate() }

    val days = summaryData.keys + measurements.keys
    val summaryJsonList = days.map { day ->
        val s = summaryData[day]?.firstOrNull()
        val json = SummarizedMeasurementJson(
            firstDay = day.toString("yyyy-MM-dd"),
            lastDay = day.toString("yyyy-MM-dd"),
            intervalType = DateIntervalType.DAY,

            minAirTemperatureCentigrade = s?.minAirTemperatureCentigrade,
            avgAirTemperatureCentigrade = s?.avgAirTemperatureCentigrade,
            maxAirTemperatureCentigrade = s?.maxAirTemperatureCentigrade,

            minDewPointTemperatureCentigrade = s?.minDewPointTemperatureCentigrade,
            avgDewPointTemperatureCentigrade = s?.avgDewPointTemperatureCentigrade,
            maxDewPointTemperatureCentigrade = s?.maxDewPointTemperatureCentigrade,

            avgWindSpeedMetersPerSecond = s?.avgWindSpeedMetersPerSecond,
            maxWindSpeedMetersPerSecond = s?.maxWindSpeedMetersPerSecond,

            sumSunshineDurationHours = s?.sumSunshineDurationHours,

            sumRainfallMillimeters = s?.sumRainfallMillimeters,
            sumSnowfallMillimeters = s?.sumSnowfallMillimeters
        )

        val h = measurements[day]?.sortedBy { it.measurementTime }
        for (hour in (0..23)) {
            json.coverages[hour] = h?.firstOrNull { it.measurementTime.hourOfDay == hour }?.cloudCoverage
        }
        return@map json
    }

    log.info { "Fetched data for station id $stationId and year $year" }
    call.respond(summaryJsonList)
}

//data class SummarizedMeasurementResponse(
//    val stationId: Long,
//    val stationName: String,
//    val measurements: List<SummarizedMeasurementJson>
//)
//
//private fun toResponse(station: Station, measurements: List<SummarizedMeasurement>): SummarizedMeasurementResponse {
//    val stations = measurements.map { it.station }.distinct()
//    return SummarizedMeasurementResponse(
//        stationId = station.id!!,
//        stationName = station.name,
//        measurements = measurements.map { it.toJson() })
//}
