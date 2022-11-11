package rootheart.codes.weatherhistory.restapp

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateIntervalType
import rootheart.codes.weatherhistory.database.HourlyMeasurementDao
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.SummarizedMeasurementDao
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val log = KotlinLogging.logger { }

fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getSummary()
}

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

@OptIn(ExperimentalTime::class)
fun Route.getSummary() = get() {
    val stationId = call.parameters["stationId"]!!.toLong()
    val year = call.request.queryParameters["year"]!!.toInt()
    val timedValue = measureTimedValue {
        log.info { "Fetching data for station id $stationId and year $year from database" }

        val station = StationDao.findById(stationId)!!

        val start = DateTime(year, 1, 1, 0, 0)
        val end = DateTime(year + 1, 1, 1, 0, 0)

        val dailyData = SummarizedMeasurementDao
            .findByStationIdAndDateBetween(station, start, end, DateIntervalType.DAY)
            .associateBy { it.firstDay.toLocalDate() }

        val yearlyData = SummarizedMeasurementDao
            .findByStationIdAndDateBetween(station, start, end, DateIntervalType.YEAR)
            .first()

        val hourlyData = HourlyMeasurementDao
            .findByStationIdAndYear(station, year)
            .groupBy { it.measurementTime.toLocalDate() }

        return@measureTimedValue YearlyData(
            year = year,
            station = station,

            minAirTemperature = yearlyData.minAirTemperatureCentigrade,
            minAirTemperatureDay = LocalDate.now(),

            avgAirTemperature = yearlyData.avgAirTemperatureCentigrade,

            maxAirTemperature = yearlyData.maxAirTemperatureCentigrade,
            maxAirTemperatureDay = LocalDate.now(),

            minAirPressureHectopascals = yearlyData.minAirPressureHectopascals,
            minAirPressureDay = LocalDate.now(),

            avgAirPressureHectopascals = yearlyData.avgAirPressureHectopascals,

            maxAirPressureHectopascals = yearlyData.maxAirPressureHectopascals,
            maxAirPressureDay = LocalDate.now(),

            avgWindSpeedMetersPerSecond = yearlyData.avgWindSpeedMetersPerSecond,
            maxWindSpeedMetersPerSecond = yearlyData.maxWindSpeedMetersPerSecond,
            maxWindSpeedDay = LocalDate.now(),

            sumRain = yearlyData.sumRainfallMillimeters,
            sumSnow = yearlyData.sumSnowfallMillimeters,
            sumSunshine = yearlyData.sumSunshineDurationHours,

            dailyData = dailyData.map { (day, data) ->
                val h = hourlyData[day]?.sortedBy { it.measurementTime }
                val coverages = ArrayList<Int?>(24)
                for (hour in (0..23)) {
                    coverages += h?.firstOrNull { it.measurementTime.hourOfDay == hour }?.cloudCoverage
                }

                DailyData(
                    day = day.toString(DATE_TIME_PATTERN),

                    minAirTemperatureCentigrade = data.minAirTemperatureCentigrade,
                    avgAirTemperatureCentigrade = data.avgAirTemperatureCentigrade,
                    maxAirTemperatureCentigrade = data.maxAirTemperatureCentigrade,

                    minDewPointTemperatureCentigrade = data.minDewPointTemperatureCentigrade,
                    maxDewPointTemperatureCentigrade = data.maxDewPointTemperatureCentigrade,
                    avgDewPointTemperatureCentigrade = data.avgDewPointTemperatureCentigrade,

                    minAirPressureHectopascals = data.minAirPressureHectopascals,
                    avgAirPressureHectopascals = data.avgAirPressureHectopascals,
                    maxAirPressureHectopascals = data.maxAirPressureHectopascals,

                    avgWindSpeedMetersPerSecond = data.avgWindSpeedMetersPerSecond,
                    maxWindSpeedMetersPerSecond = data.maxWindSpeedMetersPerSecond,

                    cloudCoverages = coverages,
                    sumSunshineDurationHours = data.sumSunshineDurationHours,
                    sumRainfallMillimeters = data.sumRainfallMillimeters,
                    sumSnowfallMillimeters = data.sumSnowfallMillimeters,
                )
            }.sortedBy { it.day }
        )
    }

    log.info { "getSummary($stationId, $year) took ${timedValue.duration.inWholeMilliseconds} millis" }
    call.respond(timedValue.value)
}

//data class SummarizedMeasurementResponse(
//    val stationId: Long,
//    val stationName: String,
//    val measurements: List<SummarizedMeasurementJson>
//)
//
//private fun toResponse(station: Station, measurements: List<SummarizedMeasurement>): SummarizedMeasurementResponse {
//    val stations = measurements.map { it.station }.distinct()
//    return SummarizedMeasurementResponse(
//        stationId = station.id!!,
//        stationName = station.name,
//        measurements = measurements.map { it.toJson() })
//}
