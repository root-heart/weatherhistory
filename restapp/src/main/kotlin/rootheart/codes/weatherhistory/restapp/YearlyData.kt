package rootheart.codes.weatherhistory.restapp

import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Station
import java.math.BigDecimal

data class YearlyData(
    val year: Int,
    val station: Station,

    val minAirTemperature: BigDecimal? = null,
    val minAirTemperatureDay: LocalDate? = null,

    val avgAirTemperature: BigDecimal? = null,

    val maxAirTemperature: BigDecimal? = null,
    val maxAirTemperatureDay: LocalDate? = null,

    val minAirPressureHectopascals: BigDecimal? = null,
    val minAirPressureDay: LocalDate? = null,

    val avgAirPressureHectopascals: BigDecimal? = null,

    val maxAirPressureHectopascals: BigDecimal? = null,
    val maxAirPressureDay: LocalDate? = null,

    val avgWindSpeedMetersPerSecond: BigDecimal? = null,
    val maxWindSpeedMetersPerSecond: BigDecimal? = null,
    val maxWindSpeedDay: LocalDate? = null,

    val sumRain: BigDecimal? = null,
    val sumSnow: BigDecimal? = null,
    val sumSunshine: BigDecimal? = null,

    val dailyData: List<DailyData> = emptyList()
)

data class DailyData(
    val day: String,

    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,

    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,

    var minAirPressureHectopascals: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var maxAirPressureHectopascals: BigDecimal? = null,

    var avgWindSpeedMetersPerSecond: BigDecimal? = null,
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,

    val cloudCoverages: MutableList<Int?> = hourList(),
    var sumSunshineDurationHours: BigDecimal? = null,
    var sumRainfallMillimeters: BigDecimal? = null,
    var sumSnowfallMillimeters: BigDecimal? = null,
)

private fun hourList(): MutableList<Int?> {
    val list = ArrayList<Int?>(24)
    list.fill(null)
    return list
}