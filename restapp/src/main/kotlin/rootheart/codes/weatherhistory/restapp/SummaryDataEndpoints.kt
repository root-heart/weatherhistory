package rootheart.codes.weatherhistory.restapp

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import rootheart.codes.common.collections.avgDecimal
import rootheart.codes.common.collections.sumDecimal
import rootheart.codes.weatherhistory.database.Measurement
import rootheart.codes.weatherhistory.database.MeasurementDao
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao
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
    val timedValue = measureTimedValue { fetchDataFromDatabase(stationId, year) }

    log.info { "getSummary($stationId, $year) took ${timedValue.duration.inWholeMilliseconds} millis" }
    call.respond(timedValue.value)
}

@OptIn(ExperimentalTime::class)
fun fetchDataFromDatabase(stationId: Long, year: Int): YearlyData {
    log.info { "Fetching data for station id $stationId and year $year from database" }

    val station = StationDao.findById(stationId)!!

    val dailyData = measureTimedValue { MeasurementDao.findByStationIdAndYear(station, year) }
        .also { log.info { "MeasurementDao.findByStationIdAndYear($stationId, $year) took ${it.duration}" } }
        .value


    val result = measureTimedValue { buildYearlyData(station, year, dailyData) }
        .also { log.info { "buildYearlyData($stationId, $year) took ${it.duration}" } }
        .value

    return result
}

private fun buildYearlyData(
    station: Station,
    year: Int,
    dailyData: List<Measurement>,
): YearlyData {
    val dayWithMinTemperature = dailyData.sortedBy { it.minAirTemperatureCentigrade }.first()
    val dayWithMaxTemperature = dailyData.sortedByDescending { it.maxAirTemperatureCentigrade }.first()

    val dayWithMinAirPressure = dailyData.sortedBy { it.minAirPressureHectopascals }.first()
    val dayWithMaxAirPressure = dailyData.sortedByDescending { it.maxAirPressureHectopascals }.first()

    val dayWithMaxWindSpeed = dailyData.sortedByDescending { it.maxWindSpeedMetersPerSecond }.first()

    return YearlyData(
        year = year,
        station = station,

        minAirTemperature = dayWithMinTemperature.minAirTemperatureCentigrade,
        minAirTemperatureDay = dayWithMinTemperature.day,

        avgAirTemperature = dailyData.avgDecimal { it.avgAirTemperatureCentigrade },

        maxAirTemperature = dayWithMaxTemperature.maxAirTemperatureCentigrade,
        maxAirTemperatureDay = dayWithMaxTemperature.day,

        minAirPressureHectopascals = dayWithMinAirPressure.minAirPressureHectopascals,
        minAirPressureDay = dayWithMinAirPressure.day,

        avgAirPressureHectopascals = dailyData.avgDecimal { it.avgAirPressureHectopascals },

        maxAirPressureHectopascals = dayWithMaxAirPressure.maxAirPressureHectopascals,
        maxAirPressureDay = dayWithMaxAirPressure.day,

        avgWindSpeedMetersPerSecond = dailyData.avgDecimal { it.avgWindSpeedMetersPerSecond },

        maxWindSpeedMetersPerSecond = dayWithMaxWindSpeed.maxWindSpeedMetersPerSecond,
        maxWindSpeedDay = dayWithMaxWindSpeed.day,

        sumRain = dailyData.sumDecimal { it.sumRainfallMillimeters },
        sumSnow = dailyData.sumDecimal { it.sumSnowfallMillimeters },
        sumSunshine = dailyData.sumDecimal { it.sumSunshineDurationHours },

        dailyData = dailyData.map {
            DailyData(
                day = it.day.toString(DATE_TIME_PATTERN),

                minAirTemperatureCentigrade = it.minAirTemperatureCentigrade,
                avgAirTemperatureCentigrade = it.avgAirTemperatureCentigrade,
                maxAirTemperatureCentigrade = it.maxAirTemperatureCentigrade,

                minDewPointTemperatureCentigrade = it.minDewPointTemperatureCentigrade,
                maxDewPointTemperatureCentigrade = it.maxDewPointTemperatureCentigrade,
                avgDewPointTemperatureCentigrade = it.avgDewPointTemperatureCentigrade,

                minAirPressureHectopascals = it.minAirPressureHectopascals,
                avgAirPressureHectopascals = it.avgAirPressureHectopascals,
                maxAirPressureHectopascals = it.maxAirPressureHectopascals,

                avgWindSpeedMetersPerSecond = it.avgWindSpeedMetersPerSecond,
                maxWindSpeedMetersPerSecond = it.maxWindSpeedMetersPerSecond,

                cloudCoverages = ArrayList(it.hourlyCloudCoverages.asList()),
                sumSunshineDurationHours = it.sumSunshineDurationHours,
                sumRainfallMillimeters = it.sumRainfallMillimeters,
                sumSnowfallMillimeters = it.sumSnowfallMillimeters,
            )
        }.sortedBy { it.day }
    )
}