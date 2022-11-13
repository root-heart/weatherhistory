package rootheart.codes.weatherhistory.restapp

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import rootheart.codes.common.collections.avgDecimal
import rootheart.codes.common.collections.sumDecimal
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.weatherhistory.database.Measurement
import rootheart.codes.weatherhistory.database.MeasurementDao
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationDao

private val log = KotlinLogging.logger { }

fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getSummary()
}

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

fun Route.getSummary() = get() {
    val yearlyData = measureAndLogDuration("Route.getSummary()") {
        val stationId = call.parameters["stationId"]!!.toLong()
        val year = call.request.queryParameters["year"]!!.toInt()
        return@measureAndLogDuration fetchDataFromDatabase(stationId, year)
    }
    call.respond(yearlyData)
}

fun fetchDataFromDatabase(stationId: Long, year: Int): YearlyData =
    measureAndLogDuration("fetchDataFromDatabase($stationId, $year)") {
        val station = StationDao.findById(stationId)!!
        val dailyData = MeasurementDao.findByStationIdAndYear(station, year)
        return@measureAndLogDuration buildYearlyData(station, year, dailyData)
    }

private fun buildYearlyData(
    station: Station,
    year: Int,
    dailyData: List<Measurement>,
): YearlyData = measureAndLogDuration("buildYearlyData(${station.id}, $year)") {
    // TODO perhaps there is a good way to reduce mapping from database result to this yearly data object
    // would reduce boilerplate code and garbage collection
    val dayWithMinTemperature =
        dailyData.filter { it.minAirTemperatureCentigrade != null }.minByOrNull { it.minAirTemperatureCentigrade!! }
    val dayWithMaxTemperature =
        dailyData.filter { it.maxAirTemperatureCentigrade != null }.maxByOrNull { it.maxAirTemperatureCentigrade!! }

    val dayWithMinAirPressure =
        dailyData.filter { it.minAirPressureHectopascals != null }.minByOrNull { it.minAirPressureHectopascals!! }
    val dayWithMaxAirPressure =
        dailyData.filter { it.maxAirPressureHectopascals != null }.maxByOrNull { it.maxAirPressureHectopascals!! }

    val dayWithMaxWindSpeed =
        dailyData.filter { it.maxWindSpeedMetersPerSecond != null }.maxByOrNull { it.maxWindSpeedMetersPerSecond!! }

    return@measureAndLogDuration YearlyData(
        year = year,
        station = station,

        minAirTemperature = dayWithMinTemperature?.minAirTemperatureCentigrade,
        minAirTemperatureDay = dayWithMinTemperature?.day,

        avgAirTemperature = dailyData.avgDecimal { it.avgAirTemperatureCentigrade },

        maxAirTemperature = dayWithMaxTemperature?.maxAirTemperatureCentigrade,
        maxAirTemperatureDay = dayWithMaxTemperature?.day,

        minAirPressureHectopascals = dayWithMinAirPressure?.minAirPressureHectopascals,
        minAirPressureDay = dayWithMinAirPressure?.day,

        avgAirPressureHectopascals = dailyData.avgDecimal { it.avgAirPressureHectopascals },

        maxAirPressureHectopascals = dayWithMaxAirPressure?.maxAirPressureHectopascals,
        maxAirPressureDay = dayWithMaxAirPressure?.day,

        avgWindSpeedMetersPerSecond = dailyData.avgDecimal { it.avgWindSpeedMetersPerSecond },

        maxWindSpeedMetersPerSecond = dayWithMaxWindSpeed?.maxWindSpeedMetersPerSecond,
        maxWindSpeedDay = dayWithMaxWindSpeed?.day,

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