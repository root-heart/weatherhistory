package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.DAO
import rootheart.codes.weatherhistory.database.HistogramDao
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.MinAvgMaxDao
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SumDao

@Serializable
@Resource("stations")
class Stations {
    @Serializable
    @Resource("{stationId}")
    class ById(private val stations: Stations, val stationId: Long) {
        @Serializable
        @Resource("{measurementType}/{year}")
        class Measurements(
            private val byId: ById,
            override val measurementType: String,
            override val year: Int,
            override val resolution: String? = "monthly",
        ) : Params() {
            override val stationId get() = byId.stationId
            override val duration get() = Interval.YEAR

            @Serializable
            @Resource("{month}")
            class ForMonth(
                private val measurements: Measurements,
                override val month: Int,
                override val resolution: String? = "daily"
            ) : Params() {
                override val stationId get() = measurements.stationId
                override val measurementType get() = measurements.measurementType
                override val year get() = measurements.year
                override val duration get() = Interval.MONTH

                @Serializable
                @Resource("{day}")
                class ForDay(
                    private val forMonth: ForMonth,
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

    val firstDay get() = LocalDate(year ?: LocalDate.now().year, month ?: 1, day ?: 1)
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

private val measurementTypeColumnsMapping: Map<String, DAO<*, out Number?>> = mapOf(
    "temperature" to MinAvgMaxDao(
        MeasurementsTable.minAirTemperatureCentigrade,
        MeasurementsTable.avgAirTemperatureCentigrade,
        MeasurementsTable.maxAirTemperatureCentigrade,
        MeasurementsTable.detailedAirTemperatureCentigrade,
    ),
    "air-pressure" to MinAvgMaxDao(
        MeasurementsTable.minAirPressureHectopascals,
        MeasurementsTable.avgAirPressureHectopascals,
        MeasurementsTable.maxAirPressureHectopascals,
        MeasurementsTable.detailedAirPressureHectopascals,
    ),
    "dew-point-temperature" to MinAvgMaxDao(
        MeasurementsTable.minDewPointTemperatureCentigrade,
        MeasurementsTable.avgDewPointTemperatureCentigrade,
        MeasurementsTable.maxDewPointTemperatureCentigrade,
        MeasurementsTable.detailedAirPressureHectopascals
    ),
    "humidity" to MinAvgMaxDao(
        MeasurementsTable.minHumidityPercent,
        MeasurementsTable.avgHumidityPercent,
        MeasurementsTable.maxHumidityPercent,
        MeasurementsTable.detailedHumidityPercent
    ),
    "visibility" to MinAvgMaxDao(
        MeasurementsTable.minVisibilityMeters,
        MeasurementsTable.avgVisibilityMeters,
        MeasurementsTable.maxVisibilityMeters,
        MeasurementsTable.detailedVisibilityMeters
    ),
    "wind-speed" to MinAvgMaxDao(
        null,
        MeasurementsTable.avgWindSpeedMetersPerSecond,
        MeasurementsTable.maxWindSpeedMetersPerSecond,
        MeasurementsTable.detailedWindSpeedMetersPerSecond
    ),
    "sunshine-duration" to SumDao(
        MeasurementsTable.sumSunshineDurationHours
    ),
    "precipitation" to SumDao(
        MeasurementsTable.sumRainfallMillimeters,
        MeasurementsTable.sumSnowfallMillimeters
    ),
    "cloud-coverage" to HistogramDao(MeasurementsTable.cloudCoverageHistogram)
)



private val requestResolutionToIntervalMapping = mapOf(
    "daily" to Interval.DAY,
    "monthly" to Interval.MONTH,
    "yearly" to Interval.YEAR
)
