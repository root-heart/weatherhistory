package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.IgnoreTrailingSlash
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MinAvgMaxDao
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.WeatherDb

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

private val formatter = org.joda.time.format.DateTimeFormat.forPattern(DATE_TIME_PATTERN)

class Ta : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter?, value: LocalDate?) {
        out?.value(formatter.print(value))
    }

    override fun read(`in`: JsonReader?): LocalDate {
        TODO("Not yet implemented")
    }

}

fun main() {
    WeatherDb.connect()
    val server = embeddedServer(Netty, port = 8080, module = Application::weatherHistory)
    server.start(wait = true)
}

fun Application.weatherHistory() {
    install(IgnoreTrailingSlash)
    install(CORS) {
        anyHost()
    }
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(LocalDate::class.java, Ta())
            setDateFormat("yyyy-MM-dd")
        }
    }
    setupRouting()
}


fun Application.setupRouting() = routing {
    trace { application.log.trace(it.buildText()) }

    static("web") {
        files(".")
    }

    route("stations") {
        get { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

        route("{stationId}") {
            get {
                val stationId = call.parameters["stationId"]!!.toLong()
                StationDao.findById(stationId)
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.NotFound, "Not Found")

            }

            measurementEndpoints()
        }
    }
}


private val requestResolutionToIntervalMapping = mapOf(
    "daily" to Interval.DAY,
    "monthly" to Interval.MONTH,
    "yearly" to Interval.YEAR
)

fun Route.measurementEndpoints() {
    get("{measurementType}/{resolution}/{year}/{month?}/{day?}") {
        val stationId = call.parameters["stationId"]!!.toLong()
        val measurementType = call.parameters["measurementType"]!!
        val resolution = call.parameters["resolution"]!!
        val year = call.parameters["year"]!!.toInt()
        val month = call.parameters["month"]?.toInt()
        val day = call.parameters["day"]?.toInt()
        val columns = measurementTypeColumnsMapping[measurementType]
        val interval = requestResolutionToIntervalMapping[resolution]
        if (columns == null || interval == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val data = MinAvgMaxDao.findAll(stationId, year, month, day, columns, interval)
            call.respond(data)
        }
    }
}
