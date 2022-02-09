package rootheart.codes.weatherhistory.rest

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import rootheart.codes.weatherhistory.database.SummarizedMeasurementDao


fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getYearlySummary()
    getMonthlySummary()
//    getDailySummary()
}

fun Route.getYearlySummary() = get("{year}") {
    val stationId = call.parameters["stationId"]!!.toInt()
    val year = call.parameters["year"]!!.toInt()
    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYear(stationId, year)
    call.respond(summarizedMeasurements)
}

fun Route.getMonthlySummary() = get("{year}/{month}") {
    val stationId = call.parameters["stationId"]!!.toInt()
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYearAndMonth(stationId, year, month)
    call.respond(summarizedMeasurements)
}

//fun Route.getDailySummary() = get("{year}") {
//    val stationId = call.parameters["stationId"]!!.toInt()
//    val year = call.parameters["year"]!!.toInt()
//    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYear(stationId, year)
//    call.respond(summarizedMeasurements)
//}

