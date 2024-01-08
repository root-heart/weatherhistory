package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import java.math.BigDecimal
import rootheart.codes.weatherhistory.database.StationDao

@Resource("stations")
class Stations

@Resource("{id}")
class StationById(val stations: Stations, val id: Long)

fun Routing.stationsResource() {
    get<Stations> {
        val stations = StationDao.findAll().sortedBy { it.federalState + it.name }
        call.respond(stations.map {
            StationJson(
                    id = it.id,
                    externalSystem = it.externalSystem,
                    externalId = it.externalId,
                    height = it.height,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    name = it.name,
                    federalState = it.federalState,
                    firstMeasurementDateString = it.firstMeasurementDate.toString("yyyy-MM-dd"),
                    lastMeasurementDateString = it.lastMeasurementDate.toString("yyyy-MM-dd")
            )
        })
    }

    get<StationById> { request ->
        call.respond(StationDao.findById(request.id) ?: HttpStatusCode.NotFound)
    }
}

data class StationJson(
        val id: Long? = null,
        val externalSystem: String,
        val externalId: String,
        val name: String,
        val federalState: String,
        val height: Int,
        val latitude: BigDecimal,
        val longitude: BigDecimal,
        val firstMeasurementDateString: String,
        val lastMeasurementDateString: String
)