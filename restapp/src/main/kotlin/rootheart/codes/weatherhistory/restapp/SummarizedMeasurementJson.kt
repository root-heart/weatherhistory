package rootheart.codes.weatherhistory.restapp

import java.math.BigDecimal

data class SummarizedMeasurementJson(
    val intervalStart: String,
    val intervalEnd: String,
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,
    var countCloudCoverage0: Int = 0,
    var countCloudCoverage1: Int = 0,
    var countCloudCoverage2: Int = 0,
    var countCloudCoverage3: Int = 0,
    var countCloudCoverage4: Int = 0,
    var countCloudCoverage5: Int = 0,
    var countCloudCoverage6: Int = 0,
    var countCloudCoverage7: Int = 0,
    var countCloudCoverage8: Int = 0,
    var countCloudCoverageNotVisible: Int = 0,
    var countCloudCoverageNotMeasured: Int = 0,
    var sumSunshineDurationHours: BigDecimal? = null,
    var sumRainfallMillimeters: BigDecimal = BigDecimal.ZERO,
    var sumSnowfallMillimeters: BigDecimal = BigDecimal.ZERO,
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var details: String? = null
)