package rootheart.codes.weatherhistory.restapp

import mu.KotlinLogging
import rootheart.codes.common.collections.avgDecimal
import rootheart.codes.common.collections.sumDecimal
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.weatherhistory.database.MeasurementDao
import rootheart.codes.weatherhistory.database.StationDao

private val log = KotlinLogging.logger { }

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

//fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
//    getSummary()
//}
//
//
//fun Route.getSummary() = get() {
//    val yearlyData = measureAndLogDuration("Route.getSummary()") {
//        val stationId = call.parameters["stationId"]!!.toLong()
//        val year = call.request.queryParameters["year"]!!.toInt()
//        return@measureAndLogDuration fetchDataFromDatabase(stationId, year)
//    }
//    call.respond(yearlyData)
//}
//
//fun fetchDataFromDatabase(stationId: Long, year: Int): YearlySummaryJson =
//    measureAndLogDuration("fetchDataFromDatabase($stationId, $year)") {
//        val station = StationDao.findById(stationId)!!
//        val dailyData = MeasurementDao.findByStationIdAndYear(station, year)
//        return@measureAndLogDuration buildYearlyData(station, year, dailyData)
//    }

fun yearlySummary(stationId: Long, year: Int) =
    measureAndLogDuration("buildYearlyData($stationId, $year)") {
        val station = StationDao.findById(stationId)!!
        val dailyData = MeasurementDao.findByStationIdAndYear(station, year)

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

        return@measureAndLogDuration YearlySummaryJson(
            year = year,

            stationId = station.id!!,
            externalSystem = station.externalSystem,
            name = station.name,
            federalState = station.federalState,
            height = station.height,
            latitude = station.latitude,
            longitude = station.longitude,

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
            sumSunshine = dailyData.sumDecimal { it.sumSunshineDurationHours }
        )
    }

//private fun buildYearlyData(
//    station: Station,
//    year: Int,
//    dailyData: List<Measurement>,
//): YearlySummaryJson = measureAndLogDuration("buildYearlyData(${station.id}, $year)") {
//    // TODO perhaps there is a good way to reduce mapping from database result to this yearly data object
//    // would reduce boilerplate code and garbage collection
//    val dayWithMinTemperature =
//        dailyData.filter { it.minAirTemperatureCentigrade != null }.minByOrNull { it.minAirTemperatureCentigrade!! }
//    val dayWithMaxTemperature =
//        dailyData.filter { it.maxAirTemperatureCentigrade != null }.maxByOrNull { it.maxAirTemperatureCentigrade!! }
//
//    val dayWithMinAirPressure =
//        dailyData.filter { it.minAirPressureHectopascals != null }.minByOrNull { it.minAirPressureHectopascals!! }
//    val dayWithMaxAirPressure =
//        dailyData.filter { it.maxAirPressureHectopascals != null }.maxByOrNull { it.maxAirPressureHectopascals!! }
//
//    val dayWithMaxWindSpeed =
//        dailyData.filter { it.maxWindSpeedMetersPerSecond != null }.maxByOrNull { it.maxWindSpeedMetersPerSecond!! }
//
//    return@measureAndLogDuration YearlySummaryJson(
//        year = year,
//        station = station,
//
//        minAirTemperature = dayWithMinTemperature?.minAirTemperatureCentigrade,
//        minAirTemperatureDay = dayWithMinTemperature?.day,
//
//        avgAirTemperature = dailyData.avgDecimal { it.avgAirTemperatureCentigrade },
//
//        maxAirTemperature = dayWithMaxTemperature?.maxAirTemperatureCentigrade,
//        maxAirTemperatureDay = dayWithMaxTemperature?.day,
//
//        minAirPressureHectopascals = dayWithMinAirPressure?.minAirPressureHectopascals,
//        minAirPressureDay = dayWithMinAirPressure?.day,
//
//        avgAirPressureHectopascals = dailyData.avgDecimal { it.avgAirPressureHectopascals },
//
//        maxAirPressureHectopascals = dayWithMaxAirPressure?.maxAirPressureHectopascals,
//        maxAirPressureDay = dayWithMaxAirPressure?.day,
//
//        avgWindSpeedMetersPerSecond = dailyData.avgDecimal { it.avgWindSpeedMetersPerSecond },
//
//        maxWindSpeedMetersPerSecond = dayWithMaxWindSpeed?.maxWindSpeedMetersPerSecond,
//        maxWindSpeedDay = dayWithMaxWindSpeed?.day,
//
//        sumRain = dailyData.sumDecimal { it.sumRainfallMillimeters },
//        sumSnow = dailyData.sumDecimal { it.sumSnowfallMillimeters },
//        sumSunshine = dailyData.sumDecimal { it.sumSunshineDurationHours },
//
//        dailyData =
//    )
//}