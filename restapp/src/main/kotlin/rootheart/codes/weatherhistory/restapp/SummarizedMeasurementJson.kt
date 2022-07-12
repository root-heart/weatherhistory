package rootheart.codes.weatherhistory.restapp

import rootheart.codes.weatherhistory.database.DateIntervalType
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import java.math.BigDecimal

data class SummarizedMeasurementJson(
    val firstDay: String,
    val lastDay: String,
    val intervalType: DateIntervalType,
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,
    val coverages: MutableList<Int?> = hourList(),
//    val sunshineDurations: MutableList<Int?> = hourList(),
    var sumSunshineDurationHours: BigDecimal? = null,
    var sumRainfallMillimeters: BigDecimal? = null,
    var sumSnowfallMillimeters: BigDecimal? = null,
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var details: String? = null
)

private fun hourList(): MutableList<Int?> {
    val list = ArrayList<Int?>()
    repeat(24) { list.add(null) }
    return list
}

//fun SummarizedMeasurement.toJson(): SummarizedMeasurementJson =
//    SummarizedMeasurementJson(
//        firstDay = interval.firstDay.toString("yyyy-MM-dd"),
//        lastDay = interval.lastDay.toString("yyyy-MM-dd"),
//        intervalType = interval.type,
//        minAirTemperatureCentigrade = minAirTemperatureCentigrade,
//        avgAirTemperatureCentigrade = avgAirTemperatureCentigrade,
//        maxAirTemperatureCentigrade = maxAirTemperatureCentigrade,
//        minDewPointTemperatureCentigrade = minDewPointTemperatureCentigrade,
//        maxDewPointTemperatureCentigrade = maxDewPointTemperatureCentigrade,
//        avgDewPointTemperatureCentigrade = avgDewPointTemperatureCentigrade,
//
//        countCloudCoverage0 = countCloudCoverage0,
//        countCloudCoverage1 = countCloudCoverage1,
//        countCloudCoverage2 = countCloudCoverage2,
//        countCloudCoverage3 = countCloudCoverage3,
//        countCloudCoverage4 = countCloudCoverage4,
//        countCloudCoverage5 = countCloudCoverage5,
//        countCloudCoverage6 = countCloudCoverage6,
//        countCloudCoverage7 = countCloudCoverage7,
//        countCloudCoverage8 = countCloudCoverage8,
//        countCloudCoverageNotVisible = countCloudCoverageNotVisible,
//        countCloudCoverageNotMeasured = countCloudCoverageNotMeasured,
//        sumSunshineDurationHours = sumSunshineDurationHours,
//        sumRainfallMillimeters = sumRainfallMillimeters,
//        sumSnowfallMillimeters = sumSnowfallMillimeters,
//        maxWindSpeedMetersPerSecond = maxWindSpeedMetersPerSecond,
//        avgWindSpeedMetersPerSecond = avgWindSpeedMetersPerSecond,
//        avgAirPressureHectopascals = avgAirPressureHectopascals,
//    )
