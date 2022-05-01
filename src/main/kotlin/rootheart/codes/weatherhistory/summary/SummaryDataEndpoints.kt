package rootheart.codes.weatherhistory.summary

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import rootheart.codes.weatherhistory.database.StationDao


fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getYearlySummary()
    getMonthlySummary()
    getDailySummary()
}

fun Route.getYearlySummary() = get("{year}") {
    val stationId = call.parameters["stationId"]!!
    val year = call.parameters["year"]!!.toInt()
    StationDao.findById(stationId.toLong())
        ?.let { station -> SummarizedMeasurementDao.findByStationIdAndYear(station.id!!, year) }
        ?.let { summarizedMeasurements -> summarizedMeasurements.map { it.toJson() } }
        ?.let { json -> call.respond(json) }
}

fun Route.getMonthlySummary() = get("{year}/{month}") {
    val stationId = call.parameters["stationId"]!!
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    StationDao.findById(stationId.toLong())
        ?.let { station -> SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station.id!!, year, month) }
        ?.let { summarizedMeasurements -> summarizedMeasurements.map { it.toJson() } }
        ?.let { json -> call.respond(json) }
}

fun Route.getDailySummary() = get("{year}/{month}/{day}") {
    call.respond("not working yet!")
//    val stationId = call.parameters["stationId"]!!.toInt()
//    val year = call.parameters["year"]!!.toInt()
//    val month = call.parameters["month"]!!.toInt()
//    val day = call.parameters["day"]!!.toInt()
//    StationDao.findById(stationId.toLong())
//        ?.let { station -> SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station.id!!, year, month, day) }
//        ?.let { summarizedMeasurements -> summarizedMeasurements.map { it.toJson() } }
//        ?.let { json -> call.respond(json) }
}
