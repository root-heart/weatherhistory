package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.summarized.MonthlySummary
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurementsTable
import rootheart.codes.weatherhistory.database.summarized.YearlySummary

@Resource("stations")
class Stations

@Resource("{id}")
class StationById(val stations: Stations, val id: Long)

fun Routing.stationsResource() {
    get<Stations> {
        call.respond(StationDao.findAll().sortedBy { it.federalState + it.name })
    }

    get<StationById> { request ->
        call.respond(StationDao.findById(request.id.toLong()) ?: HttpStatusCode.NotFound)

    }
}
