package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Interval
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
    install(CORS) { anyHost() }
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(LocalDate::class.java, Ta())
            setDateFormat("yyyy-MM-dd")
        }
    }
    install(Resources)
    setupRouting()
}

class DailyHistogram(val firstDay: LocalDate, val histogram: List<Int?>)

@Serializable
@Resource("stations")
class Stations {
    @Serializable
    @Resource("{stationId}")
    class ById(val parent: Stations = Stations(), val stationId: Long) {
        @Serializable
        @Resource("{measurementType}/{resolution}/{year}")
        class Measurements(
            val byId: ById,
            val measurementType: String,
            val resolution: String,
            val year: Int,
        ) {
            val stationId get() = byId.stationId

            @Serializable
            @Resource("{month}")
            class ForMonth(val measurements: Measurements, val month: Int) {
                val stationId get() = measurements.byId.stationId
                val measurementType get() = measurements.measurementType
                val resolution get() = measurements.resolution
                val year get() = measurements.year

                @Serializable
                @Resource("{day}")
                class ForDay(val forMonth: ForMonth, val day: Int) {
                    val stationId get() = forMonth.stationId
                    val measurementType get() = forMonth.measurementType
                    val resolution get() = forMonth.resolution
                    val year get() = forMonth.year
                    val month get() = forMonth.month
                }
            }
        }
    }
}


fun Application.setupRouting() = routing {

    get<Stations> { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

    get<Stations.ById> {
        val station = StationDao.findById(it.stationId)
        call.respond(station ?: HttpStatusCode.NotFound)
    }

    get<Stations.ById.Measurements> {
        val dao = measurementTypeColumnsMapping[it.measurementType]
        val interval = requestResolutionToIntervalMapping[it.resolution]
        if (dao == null || interval == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val data = dao.findAll(it.stationId, it.year, null, null, interval)
            call.respond(data)
        }
    }

    get<Stations.ById.Measurements.ForMonth> {
        val dao = measurementTypeColumnsMapping[it.measurementType]
        val interval = requestResolutionToIntervalMapping[it.resolution]
        if (dao == null || interval == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val data = dao.findAll(it.stationId, it.year, it.month, null, interval)
            call.respond(data)
        }
    }

    get<Stations.ById.Measurements.ForMonth.ForDay> {
        val dao = measurementTypeColumnsMapping[it.measurementType]
        val interval = requestResolutionToIntervalMapping[it.resolution]
        if (dao == null || interval == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            val data = dao.findAll(it.stationId, it.year, it.month, it.day, interval)
            call.respond(data)
        }
    }

    static("web") {
        files(".")
    }

//    route("stations") {
//        get { }
//
//        route("{stationId}") {
//            get {
//
//
//            }
//
//            get("cloudiness/{resolution}/{year}") {
//                val stationId = call.parameters["stationId"]!!.toLong()
//                val year = call.parameters["year"]!!.toInt()
//                val resolution = call.parameters["resolution"]!!
//                val interval = requestResolutionToIntervalMapping[resolution]!!
//
//                val start = LocalDate(year, 1, 1).toDateTimeAtStartOfDay()
//                val end = start.plusYears(1)
//
//                val fields = MeasurementsTable.slice(
//                    MeasurementsTable.firstDay,
//                    MeasurementsTable.cloudCoverageHistogram
//                )
//                val condition = MeasurementsTable.firstDay.greaterEq(start)
//                    .and(MeasurementsTable.firstDay.less(end))
//                    .and(MeasurementsTable.stationId.eq(stationId))
//                    .and(MeasurementsTable.interval.eq(interval))
//                val h = GeneralPurposeDao.select(fields, condition) { row ->
//                    DailyHistogram(
//                        firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
//                        histogram = row[MeasurementsTable.cloudCoverageHistogram]
//                    )
//                }
//                call.respond(h)
//            }
//        }
//    }
}


private val requestResolutionToIntervalMapping = mapOf(
    "daily" to Interval.DAY,
    "monthly" to Interval.MONTH,
    "yearly" to Interval.YEAR
)
