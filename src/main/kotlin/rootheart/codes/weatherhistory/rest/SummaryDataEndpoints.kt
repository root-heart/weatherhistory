package rootheart.codes.weatherhistory.rest

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummarizedMeasurementDao


fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getYearlySummary()
    getMonthlySummary()
    getDailySummary()
}

fun Route.getYearlySummary() = get("{year}") {
    val stationId = call.parameters["stationId"]!!.toLong()
    val station = StationDao.findById(stationId)
    val year = call.parameters["year"]!!.toInt()
    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYear(station!!.id!!, year)
    call.respond(summarizedMeasurements)
}

fun Route.getMonthlySummary() = get("{year}/{month}") {
    val stationId = call.parameters["stationId"]!!.toLong()
    val station = StationDao.findById(stationId)
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station!!.id!!, year, month)
    call.respond(summarizedMeasurements)
}

fun Route.getDailySummary() = get("{year}/{month}/{day}") {
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    val day = call.parameters["day"]!!.toInt()
    val summarizedMeasurements = SummarizedMeasurementDao.findByStationIdAndDate(stationId, year, month, day)
    call.respond(summarizedMeasurements)
}

