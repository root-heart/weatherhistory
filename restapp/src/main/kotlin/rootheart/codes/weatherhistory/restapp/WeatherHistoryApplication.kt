package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
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
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.DAO
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
        @Resource("{measurementType}/{year}")
        class Measurements(
            val byId: ById,
            override val measurementType: String,
            override val year: Int,
            override val resolution: String? = "monthly",
        ) : StationsParameters() {
            override val stationId get() = byId.stationId
            override val duration get() = Interval.YEAR

            @Serializable
            @Resource("{month}")
            class ForMonth(
                val measurements: Measurements,
                override val month: Int,
                override val resolution: String? = "daily"
            ) : StationsParameters() {
                override val stationId get() = measurements.byId.stationId
                override val measurementType get() = measurements.measurementType
                override val year get() = measurements.year
                override val duration get() = Interval.MONTH

                @Serializable
                @Resource("{day}")
                class ForDay(
                    val forMonth: ForMonth,
                    override val day: Int,
                    override val resolution: String? = "daily"
                ) : StationsParameters() {
                    override val stationId get() = forMonth.stationId
                    override val measurementType get() = forMonth.measurementType
                    override val year get() = forMonth.year
                    override val month get() = forMonth.month
                    override val duration get() = Interval.DAY
                }
            }
        }
    }
}

abstract class StationsParameters {
    open val stationId: Long = 0L
    open val measurementType: String? = null
    open val start: LocalDate? = null
    open val year: Int? = null
    open val month: Int? = null
    open val day: Int? = null
    open val resolution: String? = null
    open val duration: Interval? = null

    val firstDay get() = LocalDate(year ?: 0, month ?: 1, day ?: 1)
    val lastDay
        get() = when (duration) {
            Interval.YEAR -> firstDay.plusYears(1)
            Interval.MONTH -> firstDay.plusMonths(1)
            else -> firstDay.plusDays(1)
        }!!

}

suspend fun PipelineContext<Unit, ApplicationCall>.fetchMeasurementData(stationsParameters: StationsParameters) {
    val dao = measurementTypeColumnsMapping[stationsParameters.measurementType]
    val resolution = requestResolutionToIntervalMapping[stationsParameters.resolution]
    val data = dao?.findAll(
        stationsParameters.stationId,
        stationsParameters.firstDay,
        stationsParameters.lastDay,
        resolution ?: Interval.MONTH
    )
    call.respond(data ?: HttpStatusCode.NotFound)
}

fun Application.setupRouting() = routing {
    get<Stations> { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

    get<Stations.ById> {
        val station = StationDao.findById(it.stationId)
        call.respond(station ?: HttpStatusCode.NotFound)
    }

    get<Stations.ById.Measurements> { fetchMeasurementData(it) }

    get<Stations.ById.Measurements.ForMonth> { fetchMeasurementData(it) }

    get<Stations.ById.Measurements.ForMonth.ForDay> { fetchMeasurementData(it) }

    static("web") { files(".") }

//    route("stations") {
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
