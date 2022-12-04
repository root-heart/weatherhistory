package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
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
import org.jetbrains.exposed.sql.Column
import org.joda.time.DateTime
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.weatherhistory.database.MonthlyMinAvgMaxDao
import rootheart.codes.weatherhistory.database.MonthlySummaryTable
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummaryJdbcDao
import rootheart.codes.weatherhistory.database.WeatherDb
import rootheart.codes.weatherhistory.database.measureTransaction

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

private val formatter = org.joda.time.format.DateTimeFormat.forPattern(DATE_TIME_PATTERN)

class Ta : TypeAdapter<DateTime>() {
    override fun write(out: JsonWriter?, value: DateTime?) {
        out?.value(formatter.print(value))
    }

    override fun read(`in`: JsonReader?): DateTime {
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
            registerTypeAdapter(DateTime::class.java, Ta())
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

//            route("summary") {
//                get("{year}") {
//                }
//
//                get("{year}/{month}") {
//                }
//
//                get("{year}/{month}/{day}") {
//                }
//            }
//
//            route("temperature") {
//                yearEndpoint(
//                    "monthly", MonthlyMinAvgMaxDao(
//                        MonthlySummaryTable.minAirTemperatureCentigrade,
//                        MonthlySummaryTable.avgAirTemperatureCentigrade,
//                        MonthlySummaryTable.maxAirTemperatureCentigrade,
//                    )
//                )
//                yearEndpoint(
//                    "daily", DailyMinAvgMaxDao(
//                        MonthlySummaryTable.minAirTemperatureCentigrade,
//                        MonthlySummaryTable.avgAirTemperatureCentigrade,
//                        MonthlySummaryTable.maxAirTemperatureCentigrade,
//                    )
//                )
//
//
//                monthlyEndpoint(MonthlyTemperatureDao)
//                dailyEndpoints(DailyTemperatureDao)
//                hourlyEndpoints(HourlyTemperatureDao)
//            }
//
//            route("dewPointTemperature") {
//                monthlyEndpoint(MonthlyDewPointTemperatureDao)
//                dailyEndpoints(DailyDewPointTemperatureDao)
//                hourlyEndpoints(HourlyDewPointTemperatureDao)
//            }
//
//            route("humidity") {
//                monthlyEndpoint(MonthlyHumidityDao)
//                dailyEndpoints(DailyHumidityDao)
//                hourlyEndpoints(HourlyHumidityDao)
//            }
//
//            route("airPressure") {
//                monthlyEndpoint(MonthlyAirPressureDao)
//                dailyEndpoints(DailyAirPressureDao)
//                hourlyEndpoints(HourlyAirPressureDao)
//            }
//
//            route("cloudCoverage") {
//                hourlyEndpoints(HourlyCoverageDao)
//            }
//
//            route("sunshine") {
//                monthlyEndpoint(MonthlySunshineDurationDao)
//                dailyEndpoints(DailySunshineDurationDao)
//                hourlyEndpoints(HourlySunshineDurationDao)
//            }
//
//            route("precipitation") {
//                monthlyEndpoint(MonthlyPrecipitationDao)
//                dailyEndpoints(DailyPrecipitationDao)
//                hourlyEndpoints(HourlyPrecipitationDao)
//            }
//
//            route("wind") {
//                monthlyEndpoint(MonthlyWindDao)
//                dailyEndpoints(DailyWindDao)
//                hourlyEndpoints(HourlyWindDao)
//            }
//
//            route("visibility") {
//                monthlyEndpoint(MonthlyVisibilityDao)
//                hourlyEndpoints(HourlyVisibilityDao)
//            }
        }
    }
}

private data class MeasurementColumns(
    private val minColumn: Column<out Number?>,
    private val avgColumn: Column<out Number?>,
    private val maxColumn: Column<out Number?>,
)

private val measurementEndpointNames = mapOf(
    "temperature" to listOf(
        MonthlySummaryTable.minAirTemperatureCentigrade,
        MonthlySummaryTable.avgAirTemperatureCentigrade,
        MonthlySummaryTable.maxAirTemperatureCentigrade,
    ),
    "air-pressure" to listOf(
        MonthlySummaryTable.minAirPressureHectopascals,
        MonthlySummaryTable.avgAirPressureHectopascals,
        MonthlySummaryTable.maxAirPressureHectopascals,
    )
)

fun Route.measurementEndpoints() {
    get("{measurementType}/{resolution}/{year}/{month?}/{day?}") {

        val measurementType = call.parameters["measurementType"]
        val columns = measurementEndpointNames[measurementType]
        if (columns == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {

        }
    }
}

fun Route.monthlyEndpoint(dao: SummaryJdbcDao) {
    get("monthly/{year}") {
        val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
        val stationId = call.parameters["stationId"]!!.toLong()
        val year = call.parameters["year"]!!.toInt()
        measureAndLogDuration(identifier) {
            StationDao.findById(stationId)
                ?.let { dao.fetchFromDb(stationId, year) }
                ?.let { measureAndLogDuration("Responding to $identifier") { call.respond(it) } }
                ?: call.respond(HttpStatusCode.NotFound)
        }

    }
}

fun Route.monthlyEndpoint(dao: MonthlyMinAvgMaxDao) {
    get("monthly/{year}") {
        val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
        val stationId = call.parameters["stationId"]!!.toLong()
        val year = call.parameters["year"]!!.toInt()
        measureAndLogDuration(identifier) {
            StationDao.findById(stationId)
                ?.let { dao.findByStationIdAndYear(stationId, year) }
                ?.let { measureAndLogDuration("Responding to $identifier") { call.respond(it) } }
                ?: call.respond(HttpStatusCode.NotFound)
        }

    }
}

//fun Route.dailyEndpoints(dao: Dao) {
////    get("daily/{year}") { yearEndpoint(dao) }
//    get("daily/{year}/{month}") { monthEndpoint(dao) }
//}
//
//fun Route.dailyEndpoints(dao: DailyMinAvgMaxDao) {
//    get("daily/{year}") { yearEndpoint(dao::findByStationIdAndYear) }
////    get("daily/{year}/{month}") { monthEndpoint(dao) }
//}

//fun Route.hourlyEndpoints(dao: Dao) {
////    get("hourly/{year}") { yearEndpoint(dao::findByYear) }
//    get("hourly/{year}/{month}") { monthEndpoint(dao) }
//    get("hourly/{year}/{month}/{day}") { dayEndpoint(dao) }
//}

suspend fun <T> PipelineContext<Unit, ApplicationCall>.yearEndpoint(findMethod: (Long, Int) -> List<T>) {
    val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.parameters["year"]!!.toInt()
    val list = measureTransaction(identifier) {
        val station = StationDao.findById(stationId)
        if (station != null) findMethod(stationId, year) else emptyList()
    }
    call.respond(list)
}

suspend fun <T> PipelineContext<Unit, ApplicationCall>.monthEndpoint(findMethod: (Long, Int, Int) -> List<T>) {
    val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    val list = measureTransaction(identifier) {
        val station = StationDao.findById(stationId)
        if (station != null) findMethod(stationId, year, month) else emptyList()
    }
    call.respond(list)
}

//suspend fun PipelineContext<Unit, ApplicationCall>.dayEndpoint(dao: Dao) {
//    val identifier = "${call.request.httpMethod.value} ${call.request.uri}"
//    val stationId = call.parameters["stationId"]!!.toLong()
//    val year = call.parameters["year"]!!.toInt()
//    val month = call.parameters["month"]!!.toInt()
//    val day = call.parameters["day"]!!.toInt()
//    measureTransaction(identifier) {
//        StationDao.findById(stationId)
//            ?.let { station -> dao.findByYearMonthAndDay(station, year, month, day) }
//            ?.let { measureAndLogDuration("Responding to $identifier") { call.respond(it) } }
//            ?: call.respond(HttpStatusCode.NotFound)
//    }
//}