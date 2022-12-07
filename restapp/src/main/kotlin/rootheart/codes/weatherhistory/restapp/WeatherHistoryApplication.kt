package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.GeneralPurposeDao
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementsTable
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
//    install(Resources)
    setupRouting()
}

class DailyHistogram(val firstDay: LocalDate, val histogram: List<Int?>)

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

            get("cloudiness/{resolution}/{year}") {
                val stationId = call.parameters["stationId"]!!.toLong()
                val year = call.parameters["year"]!!.toInt()
                val resolution = call.parameters["resolution"]!!
                val interval = requestResolutionToIntervalMapping[resolution]!!

                val start = LocalDate(year, 1, 1).toDateTimeAtStartOfDay()
                val end = start.plusYears(1)

                val fields = MeasurementsTable.slice(
                    MeasurementsTable.firstDay,
                    MeasurementsTable.cloudCoverageHistogram
                )
                val condition = MeasurementsTable.firstDay.greaterEq(start)
                    .and(MeasurementsTable.firstDay.less(end))
                    .and(MeasurementsTable.stationId.eq(stationId))
                    .and(MeasurementsTable.interval.eq(interval))
                val h = GeneralPurposeDao.select(fields, condition) { row ->
                    DailyHistogram(
                        firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
                        histogram = row[MeasurementsTable.cloudCoverageHistogram]
                    )
                }
                call.respond(h)
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
        val dao = measurementTypeColumnsMapping[measurementType]
        val interval = requestResolutionToIntervalMapping[resolution]
        if (dao == null || interval == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val data = dao.findAll(stationId, year, month, day, interval)
            call.respond(data)
        }
    }
}
