package rootheart.codes.weatherhistory.restapp

import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Station
import java.math.BigDecimal

data class YearlyData(
    val year: Int,
    val station: Station,

    val minAirTemperature: BigDecimal?,
    val minAirTemperatureDay: LocalDate?,

    val avgAirTemperature: BigDecimal?,

    val maxAirTemperature: BigDecimal?,
    val maxAirTemperatureDay: LocalDate?,

    val minAirPressureHectopascals: BigDecimal?,
    val minAirPressureDay: LocalDate?,

    val avgAirPressureHectopascals: BigDecimal?,

    val maxAirPressureHectopascals: BigDecimal?,
    val maxAirPressureDay: LocalDate?,

    val avgWindSpeedMetersPerSecond: BigDecimal?,
    val maxWindSpeedMetersPerSecond: BigDecimal?,
    val maxWindSpeedDay: LocalDate?,

    val sumRain: BigDecimal?,
    val sumSnow: BigDecimal?,
    val sumSunshine: BigDecimal?,

    val dailyData: List<DailyData>
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