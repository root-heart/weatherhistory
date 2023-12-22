package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import java.io.Serializable
import kotlinx.serialization.SerialName
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import rootheart.codes.weatherhistory.database.daily.DailyAvgMaxColumns
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementTable
import rootheart.codes.weatherhistory.database.daily.DailyMinAvgMaxColumns
import rootheart.codes.weatherhistory.database.daily.DailySumColumns

private const val airTemperatureResourceName = "air-temperature"
private const val humidityResourceName = "humidity"
private const val airPressureResourceName = "air-pressure"
private const val visibilityResourceName = "visibility"
private const val sunshineResourceName = "sunshine"
private const val rainResourceName = "rain"
private const val snowResourceName = "snow"
private const val dewPointTemperatureResourceName = "dew-point-temperature"
private const val windSpeedResourceName = "wind-speed"
private const val cloudCoverageResourceName = "cloud-coverage"

enum class Measurement(val columns: Array<Column<out Serializable?>>, val dataMapper: (ResultRow) -> Any) {
    @SerialName(airTemperatureResourceName)
    AIR_TEMPERATURE(DailyMeasurementTable.airTemperatureCentigrade),

    @SerialName(dewPointTemperatureResourceName)
    DEW_POINT_TEMPERATURE(DailyMeasurementTable.dewPointTemperatureCentigrade),

    @SerialName(humidityResourceName)
    HUMIDITY(DailyMeasurementTable.humidityPercent),

    @SerialName(airPressureResourceName)
    AIR_PRESSURE(DailyMeasurementTable.airPressureHectopascals),

    @SerialName(visibilityResourceName)
    VISIBILITY(DailyMeasurementTable.visibilityMeters),

    @SerialName(sunshineResourceName)
    SUNSHINE(DailyMeasurementTable.sunshineMinutes),

    @SerialName(rainResourceName)
    RAIN(DailyMeasurementTable.rainfallMillimeters),

    @SerialName(windSpeedResourceName)
    WIND_SPEED(DailyMeasurementTable.windSpeedMetersPerSecond),

    @SerialName(snowResourceName)
    SNOW(DailyMeasurementTable.snowfallMillimeters);

    constructor(columns: DailyMinAvgMaxColumns)
            : this(arrayOf(DailyMeasurementTable.date, columns.min, columns.avg, columns.max),
                   {
                       arrayOf(it[DailyMeasurementTable.date].millis,
                               it[columns.min],
                               it[columns.avg],
                               it[columns.max])
                   })

    constructor(columns: DailyAvgMaxColumns)
            : this(arrayOf(DailyMeasurementTable.date, columns.avg, columns.max),
                   {
                       arrayOf(it[DailyMeasurementTable.date].millis,
                               it[columns.avg],
                               it[columns.max])
                   })

    constructor(columns: DailySumColumns<*>)
            : this(arrayOf(DailyMeasurementTable.date, columns.sum),
                   { arrayOf(it[DailyMeasurementTable.date].millis, it[columns.sum]) })
}

enum class DetailedMeasurement(val column: Column<out Array<out Serializable?>?>) {
    @SerialName(airTemperatureResourceName)
    AIR_TEMPERATURE(DailyMeasurementTable.airTemperatureCentigrade.details),

    @SerialName(humidityResourceName)
    HUMIDITY(DailyMeasurementTable.humidityPercent.details),

    @SerialName(dewPointTemperatureResourceName)
    DEW_POINT_TEMPERATURE(DailyMeasurementTable.dewPointTemperatureCentigrade.details),

    @SerialName(visibilityResourceName)
    VISIBILITY(DailyMeasurementTable.visibilityMeters.details),

    @SerialName(sunshineResourceName)
    SUNSHINE(DailyMeasurementTable.sunshineMinutes.details),

    @SerialName(windSpeedResourceName)
    WIND_SPEED(DailyMeasurementTable.windSpeedMetersPerSecond.details),

    @SerialName(cloudCoverageResourceName)
    CLOUD_COVERAGE(DailyMeasurementTable.detailedCloudCoverage);
}

@Resource("{measurement}/{year}")
class MeasurementResource(val stationById: StationById, val measurement: Measurement, val year: Int)

@Resource("{measurement}/details/{year}")
class DetailedMeasurementsResource(val stationById: StationById, val measurement: DetailedMeasurement, val year: Int)

@Resource("sunshine-cloud-coverage/{year}")
class SunshineCloudCoverage(val stationById: StationById, val year: Int)

fun Routing.measurementsResource() {
    get<MeasurementResource> { request ->
        val data = with(request) {
            DailyMeasurementTable.fetchData(measurement.columns, stationById.id, year, measurement.dataMapper)
        }
        call.respond(data)
    }

    get<DetailedMeasurementsResource> { request ->
        val data = with(request) {
            val columns = arrayOf(DailyMeasurementTable.date, measurement.column)
            DailyMeasurementTable.fetchData(columns, stationById.id, year) {
                arrayOf(it[DailyMeasurementTable.date].millis, it[measurement.column])
            }
        }
        call.respond(data)
    }

    get<SunshineCloudCoverage> { m ->
        val histogramData = with(DailyMeasurementTable) {
            fetchData(arrayOf(date, detailedCloudCoverage, sunshineMinutes.details), m.stationById.id, m.year) {
                val cloudCoverages = it[detailedCloudCoverage]
                val sunshineDurations = it[sunshineMinutes.details]
                if (cloudCoverages != null && sunshineDurations != null) {
                    val sunshineCloudCoverage =
                        multiplyCloudCoverageAndSunshineDuration(cloudCoverages, sunshineDurations)
                    arrayOf(it[date].millis, sunshineCloudCoverage)
                } else {
                    arrayOf(it[date].millis, null)
                }
            }
        }
        call.respond(histogramData)
    }
}

private fun multiplyCloudCoverageAndSunshineDuration(
        cloudCoverages: Array<Int?>,
        sunshineDurations: Array<Int?>
): Array<Int?> {
    val sunshineCloudCoverage = Array<Int?>(24) { null }
    for (hour in 0..23) {
        val cloudCoverage = cloudCoverages[hour]
        val sunshineDuration = sunshineDurations[hour] ?: 0
        if (cloudCoverage != null) {
            sunshineCloudCoverage[hour] = (8 - cloudCoverage) * sunshineDuration / 8
        }
    }
    return sunshineCloudCoverage
}