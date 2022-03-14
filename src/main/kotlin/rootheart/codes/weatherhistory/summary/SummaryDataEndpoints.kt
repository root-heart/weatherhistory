package rootheart.codes.weatherhistory.summary

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import rootheart.codes.weatherhistory.database.StationDao


fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getYearlySummary()
    getMonthlySummary()
//    getDailySummary()
}

fun Route.getYearlySummary() = get("{year}") {
    val stationId = call.parameters["stationId"]!!.toLong()
    val station = StationDao.findById(stationId)
    val year = call.parameters["year"]!!.toInt()
    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYear(station!!.id!!, year).map { it.toJson() }
    call.respond(summarizedMeasurements)
}

fun Route.getMonthlySummary() = get("{year}/{month}") {
    val stationId = call.parameters["stationId"]!!
    val station = StationDao.findStationByExternalId(stationId)
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station!!.id!!, year, month)
    call.respond(summarizedMeasurements)
}

//fun Route.getDailySummary() = get("{year}") {
//    val stationId = call.parameters["stationId"]!!.toInt()
//    val year = call.parameters["year"]!!.toInt()
//    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYear(stationId, year)
//    call.respond(summarizedMeasurements)
//}

