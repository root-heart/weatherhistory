package rootheart.codes.weatherhistory.restapp

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.IgnoreTrailingSlash
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.weatherhistory.database.Dao
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.WeatherDb

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

fun main() {
    WeatherDb.connect()
    val server = embeddedServer(Netty, port = 8080) {
        install(IgnoreTrailingSlash)
        install(CORS) {
            anyHost()
        }
        install(ContentNegotiation) { gson() }
        setupRouting()
    }
    server.start(wait = true)
}

fun Application.setupRouting() = routing {
    trace { application.log.trace(it.buildText()) }

    static("web") {
        files(".")
    }

    get("stations") {
        call.respond(StationDao.findAll().sortedBy { it.federalState + it.name })
    }

    get("stations/{stationId}") {
        val stationId = call.parameters["stationId"]!!.toLong()
        StationDao.findById(stationId)
            ?.let { call.respond(it) }
            ?: call.respond(HttpStatusCode.NotFound, "Not Found")
    }

    route("summary/{stationId}") {
        get("{year}") {
            val stationId = call.parameters["stationId"]!!.toLong()
            val year = call.parameters["year"]!!.toInt()
//            val summary = yearlySummary(stationId, year)
            call.respond(HttpStatusCode.Gone)
        }

        get("{year}/{month}") {
        }

        get("{year}/{month}/{day}") {
        }
    }

    route("temperature/{stationId}") {
        dailyEndpoints(DailyTemperatureDao)
        hourlyEndpoints(HourlyTemperatureDao)
    }

    route("dewPointTemperature") {
        // see "temperature" route above
    }

    route("humidity") {
        // see "temperature" route above
    }

    route("airPressure") {
        // see "temperature" route above
    }

    route("cloudCoverage/{stationId}") {
        hourlyEndpoints(HourlyCoverageDao)
    }

    route("sunshine/{stationId}") {
        dailyEndpoints(DailySunshineDurationDao)
        hourlyEndpoints(HourlySunshineDurationDao)
    }

    route("rainfall") {
        // see "temperature" route above
    }

    route("snowfall") {
        // see "temperature" route above
    }

    route("wind") {
        // see "temperature" route above
    }

    route("visibility") {
        // see "temperature" route above
    }

    stationsEndpoints()
//    summaryDataEndpoints()
//    measurementEndpoints()
}

fun Route.dailyEndpoints(dao: Dao) {
    get("daily/{year}") { yearEndpoint(dao) }
    get("daily/{year}/{month}") { monthEndpoint(dao) }
}

fun Route.hourlyEndpoints(dao: Dao) {
    get("hourly/{year}") { yearEndpoint(dao) }
    get("hourly/{year}/{month}") { monthEndpoint(dao) }
    get("hourly/{year}/{month}/{day}") { dayEndpoint(dao) }
}

suspend fun PipelineContext<Unit, ApplicationCall>.yearEndpoint(dao: Dao) {
    val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.parameters["year"]!!.toInt()
    measureAndLogDuration(identifier) {
        StationDao.findById(stationId)
            ?.let { station -> dao.findByYear(station, year) }
            ?.let { measureAndLogDuration("Responding to $identifier") { call.respond(it) } }
            ?: call.respond(HttpStatusCode.NotFound)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.monthEndpoint(dao: Dao) {
    val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    measureAndLogDuration(identifier) {
        StationDao.findById(stationId)
            ?.let { station -> dao.findByYearAndMonth(station, year, month) }
            ?.let { measureAndLogDuration("Responding to $identifier") { call.respond(it) } }
            ?: call.respond(HttpStatusCode.NotFound)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.dayEndpoint(dao: Dao) {
    val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    val day = call.parameters["day"]!!.toInt()
    measureAndLogDuration(identifier) {
        StationDao.findById(stationId)
            ?.let { station -> dao.findByYearMonthAndDay(station, year, month, day) }
            ?.let { measureAndLogDuration("Responding to $identifier") { call.respond(it) } }
            ?: call.respond(HttpStatusCode.NotFound)
    }
}