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
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateIntervalType
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.SummarizedMeasurementDao

private val log = KotlinLogging.logger { }

fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
//    getYearlySummary()
//    getMonthlySummary()
//    getDailySummary()
    getSummary()
}

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC()

fun Route.getSummary() = get() {
    val stationId = call.parameters["stationId"]!!.toLong()
    val from = LocalDate.parse(call.request.queryParameters["from"]!!, DATE_TIME_FORMATTER)
    val to = LocalDate.parse(call.request.queryParameters["to"]!!, DATE_TIME_FORMATTER)

    log.info { "Fetching data for station id $stationId and date range $from - $to from database" }

    val period = Period.fieldDifference(from, to)
    var intervalType = DateIntervalType.DAY
    if (period.years > 100) {
        intervalType = DateIntervalType.DECADE
    } else if (period.years > 30) {
        intervalType = DateIntervalType.YEAR
    } else if (period.years > 10) {
        intervalType = DateIntervalType.SEASON
    } else if (period.years > 0 || period.months > 3) {
        intervalType = DateIntervalType.MONTH
    }

    val station = StationDao.findById(stationId)!!
    val summaryData = SummarizedMeasurementDao.findByStationIdAndDateBetween(
        station,
        from.toDateTimeAtStartOfDay(),
        to.toDateTimeAtStartOfDay(),
        intervalType
    )

    val summaryJsonList = summaryData.map { it.toJson() }
    log.info("Fetched data for station id {} and date range {} - {}", stationId, from, to)
    call.respond(summaryJsonList)
}

//fun Route.getYearlySummary() = get("{year}") {
//    val stationId = call.parameters["stationId"]!!
//    val year = call.parameters["year"]!!.toInt()
//    val station = StationDao.findById(stationId.toLong())
//    if (station != null) {
//        val measurements = SummarizedMeasurementDao.findByStationIdAndYear(station, year)
//        val response = toResponse(station, measurements)
//        call.respond(response)
//    } else {
//        call.respond(HttpStatusCode.NotFound)
//    }
//
//}

//fun Route.getMonthlySummary() = get("{year}/{month}") {
//    val stationId = call.parameters["stationId"]!!
//    val year = call.parameters["year"]!!.toInt()
//    val month = call.parameters["month"]!!.toInt()
//    val station = StationDao.findById(stationId.toLong())
//    if (station != null) {
//        val measurements = SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station, year, month)
//        val response = toResponse(station, measurements)
//        call.respond(response)
//    } else {
//        call.respond(HttpStatusCode.NotFound)
//    }
//}

//fun Route.getDailySummary() = get("{year}/{month}/{day}") {
//    call.respond("not working yet!")
////    val stationId = call.parameters["stationId"]!!.toInt()
////    val year = call.parameters["year"]!!.toInt()
////    val month = call.parameters["month"]!!.toInt()
////    val day = call.parameters["day"]!!.toInt()
////    StationDao.findById(stationId.toLong())
////        ?.let { station -> SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station.id!!, year, month, day) }
////        ?.let { summarizedMeasurements -> summarizedMeasurements.map { it.toJson() } }
////        ?.let { json -> call.respond(json) }
//}


data class SummarizedMeasurementResponse(
    val stationId: Long,
    val stationName: String,
    val measurements: List<SummarizedMeasurementJson>
)

private fun toResponse(station: Station, measurements: List<SummarizedMeasurement>): SummarizedMeasurementResponse {
    val stations = measurements.map { it.station }.distinct()
    return SummarizedMeasurementResponse(
        stationId = station.id!!,
        stationName = station.name,
        measurements = measurements.map { it.toJson() })
}
