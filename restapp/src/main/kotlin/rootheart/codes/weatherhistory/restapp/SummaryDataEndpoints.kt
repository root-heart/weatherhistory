package rootheart.codes.weatherhistory.summary

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.rest.SummarizedMeasurementJson


fun Routing.summaryDataEndpoints() = route("summary/{stationId}") {
    getYearlySummary()
    getMonthlySummary()
    getDailySummary()
}

fun Route.getYearlySummary() = get("{year}") {
    val stationId = call.parameters["stationId"]!!
    val year = call.parameters["year"]!!.toInt()
    StationDao.findById(stationId.toLong())
        ?.let { station -> SummarizedMeasurementDao.findByStationIdAndYear(station, year) }
        ?.let { summarizedMeasurements -> summarizedMeasurements.map { it.toJson() } }
        ?.let { json -> call.respond(json) }
}

fun Route.getMonthlySummary() = get("{year}/{month}") {
    val stationId = call.parameters["stationId"]!!
    val year = call.parameters["year"]!!.toInt()
    val month = call.parameters["month"]!!.toInt()
    StationDao.findById(stationId.toLong())
        ?.let { station -> SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station, year, month) }
        ?.let { summarizedMeasurements -> summarizedMeasurements.map { it.toJson() } }
//        ?.map {  }
        ?.let { json -> call.respond(json) }
}

fun Route.getDailySummary() = get("{year}/{month}/{day}") {
    call.respond("not working yet!")
//    val stationId = call.parameters["stationId"]!!.toInt()
//    val year = call.parameters["year"]!!.toInt()
//    val month = call.parameters["month"]!!.toInt()
//    val day = call.parameters["day"]!!.toInt()
//    StationDao.findById(stationId.toLong())
//        ?.let { station -> SummarizedMeasurementDao.findByStationIdAndYearAndMonth(station.id!!, year, month, day) }
//        ?.let { summarizedMeasurements -> summarizedMeasurements.map { it.toJson() } }
//        ?.let { json -> call.respond(json) }
}


data class SummarizedMeasurementResponse(
    val stationId: Long,
    val stationName: String,
    val measurements: List<SummarizedMeasurementJson>
)

private fun toResponse(measurements: List<SummarizedMeasurement>): SummarizedMeasurementResponse {
    val stations = measurements.map { it.station }.distinct()
    return SummarizedMeasurementResponse(
        stationId = stations.first().id!!,
        stationName = stations.first().name,
        measurements = measurements.map { it.toJson() })
}

private fun SummarizedMeasurement.toJson(): SummarizedMeasurementJson {
    return SummarizedMeasurementJson(
        intervalStart = firstDay.toString("yyyyMMdd"),
        intervalEnd = lastDay.toString("yyyyMMdd"),
        minAirTemperatureCentigrade = minAirTemperatureCentigrade,
        avgAirTemperatureCentigrade = avgAirTemperatureCentigrade,
        maxAirTemperatureCentigrade = maxAirTemperatureCentigrade,
        minDewPointTemperatureCentigrade = minDewPointTemperatureCentigrade,
        maxDewPointTemperatureCentigrade = maxDewPointTemperatureCentigrade,
        avgDewPointTemperatureCentigrade = avgDewPointTemperatureCentigrade,
        countCloudCoverage0 = countCloudCoverage0,
        countCloudCoverage1 = countCloudCoverage1,
        countCloudCoverage2 = countCloudCoverage2,
        countCloudCoverage3 = countCloudCoverage3,
        countCloudCoverage4 = countCloudCoverage4,
        countCloudCoverage5 = countCloudCoverage5,
        countCloudCoverage6 = countCloudCoverage6,
        countCloudCoverage7 = countCloudCoverage7,
        countCloudCoverage8 = countCloudCoverage8,
        countCloudCoverageNotVisible = countCloudCoverageNotVisible,
        countCloudCoverageNotMeasured = countCloudCoverageNotMeasured,
        sumSunshineDurationHours = sumSunshineDurationHours,
        sumRainfallMillimeters = sumRainfallMillimeters,
        sumSnowfallMillimeters = sumSnowfallMillimeters,
        maxWindSpeedMetersPerSecond = maxWindSpeedMetersPerSecond,
        avgWindSpeedMetersPerSecond = avgWindSpeedMetersPerSecond,
        avgAirPressureHectopascals = avgAirPressureHectopascals,
        details = details
    )
}