package rootheart.codes.weatherhistory.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import rootheart.codes.weatherhistory.database.StationDao

fun Routing.stationsEndpoints() = route("stations") {
    getAllStations()
    getStationByStationId()
}

fun Route.getAllStations() = get {
    call.respond(StationDao.findAll())
}

fun Route.getStationByStationId() = get("{stationId}") {
    val stationId = call.parameters["stationId"]!!
    StationDao.findById(stationId.toLong())
        ?.let { call.respond(it) }
        ?: call.respond(HttpStatusCode.NotFound, "Not Found")
}
