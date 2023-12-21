package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import rootheart.codes.weatherhistory.database.StationDao

@Resource("stations")
class Stations

@Resource("{id}")
class StationById(val stations: Stations, val id: Long)

fun Routing.stationsResource() {
    get<Stations> {
        call.respond(StationDao.findAll().sortedBy { it.federalState + it.name })
    }

    get<StationById> { request ->
        call.respond(StationDao.findById(request.id) ?: HttpStatusCode.NotFound)

    }
}
