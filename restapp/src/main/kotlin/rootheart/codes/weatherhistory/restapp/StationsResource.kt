package rootheart.codes.weatherhistory.restapp

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.StationDao

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
        ) : Params() {
            override val stationId get() = byId.stationId
            override val duration get() = Interval.YEAR

            @Serializable
            @Resource("{month}")
            class ForMonth(
                val measurements: Measurements,
                override val month: Int,
                override val resolution: String? = "daily"
            ) : Params() {
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
                ) : Params() {
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

fun Routing.stationsResource() {
    get<Stations> { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }
    get<Stations.ById> { call.respond(StationDao.findById(it.stationId) ?: HttpStatusCode.NotFound) }
    get<Stations.ById.Measurements> { fetchMeasurementData(it) }
    get<Stations.ById.Measurements.ForMonth> { fetchMeasurementData(it) }
    get<Stations.ById.Measurements.ForMonth.ForDay> { fetchMeasurementData(it) }
}

abstract class Params {
    open val stationId: Long = 0L
    open val measurementType: String? = null
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

private suspend fun PipelineContext<Unit, ApplicationCall>.fetchMeasurementData(params: Params) {
    val dao = measurementTypeColumnsMapping[params.measurementType]
    val resolution = requestResolutionToIntervalMapping[params.resolution]
    val data = dao?.findAll(
        params.stationId,
        params.firstDay,
        params.lastDay,
        resolution ?: Interval.MONTH
    )
    call.respond(data ?: HttpStatusCode.NotFound)
}

private val requestResolutionToIntervalMapping = mapOf(
    "daily" to Interval.DAY,
    "monthly" to Interval.MONTH,
    "yearly" to Interval.YEAR
)
