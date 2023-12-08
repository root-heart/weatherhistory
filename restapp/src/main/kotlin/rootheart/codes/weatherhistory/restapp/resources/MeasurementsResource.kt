package rootheart.codes.weatherhistory.restapp.resources

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementDao
import rootheart.codes.weatherhistory.restapp.NumberInterval
import rootheart.codes.weatherhistory.restapp.optQueryParam
import rootheart.codes.weatherhistory.restapp.requiredPathParam
import rootheart.codes.weatherhistory.restapp.toInterval

fun Routing.measurementsResource() {
    route("/measurements/{stationId}") {

        get {
            call.respond(HttpStatusCode.NotImplemented)
        }

        get("/{years}") {
            val years = requiredPathParam("years") { toInterval(it) }
            val stationId = requiredPathParam("stationId") { it.toLong() }
            val resolution = optQueryParam("resolution")

            if (resolution == "month") {
                call.respond(HttpStatusCode.NotImplemented)
            } else {
                fetchDailyMeasurements(stationId, years)

//                call.respond(...)
            }
        }
    }
}

private fun fetchDailyMeasurements(stationId: Long, years: NumberInterval) {
    with(DailyMeasurementDao) {
        val firstAndLastDay = getFirstAndLastDay(stationId, years.start, years.end)
        if (firstAndLastDay.monthsCount <= 12) {
            fetchMeasurements(stationId, years.start, years.end) {
//                it.toJson()
            }
        }
    }
}
