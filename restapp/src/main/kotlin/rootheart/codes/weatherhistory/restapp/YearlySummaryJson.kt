package rootheart.codes.weatherhistory.restapp

import org.joda.time.LocalDate
import java.math.BigDecimal

data class YearlySummaryJson(
    val year: Int,

    val stationId: Long,
    val externalSystem: String,
    val name: String,
    val federalState: String,
    val height: Int,
    val latitude: BigDecimal,
    val longitude: BigDecimal,


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

//    val dailyData: List<MeasurementJson> = emptyList()
)


data class MeasurementJson(
    val day: String,

    var hourlyAirTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,

    var hourlyDewPointTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,

    var hourlyHumidityPercent: Array<BigDecimal?> = Array(24) { null },
    var minHumidityPercent: BigDecimal? = null,
    var maxHumidityPercent: BigDecimal? = null,
    var avgHumidityPercent: BigDecimal? = null,

    var hourlyAirPressureHectopascals: Array<BigDecimal?> = Array(24) { null },
    var minAirPressureHectopascals: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var maxAirPressureHectopascals: BigDecimal? = null,

    var hourlyCloudCoverages: Array<Int?> = Array(24) { null },

    var hourlySunshineDurationMinutes: Array<Int?> = Array(24) { null },
    var sumSunshineDurationHours: BigDecimal? = null,

    var hourlyRainfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumRainfallMillimeters: BigDecimal? = null,

    var hourlySnowfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumSnowfallMillimeters: BigDecimal? = null,

    var hourlyWindSpeedMetersPerSecond: Array<BigDecimal?> = Array(24) { null },
    var hourlyWindDirectionDegrees: Array<Int?> = Array(24) { null },
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,

    var hourlyVisibilityMeters: Array<Int?> = Array(24) { null },
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MeasurementJson

        if (day != other.day) return false
        if (!hourlyAirTemperatureCentigrade.contentEquals(other.hourlyAirTemperatureCentigrade)) return false
        if (minAirTemperatureCentigrade != other.minAirTemperatureCentigrade) return false
        if (avgAirTemperatureCentigrade != other.avgAirTemperatureCentigrade) return false
        if (maxAirTemperatureCentigrade != other.maxAirTemperatureCentigrade) return false
        if (!hourlyDewPointTemperatureCentigrade.contentEquals(other.hourlyDewPointTemperatureCentigrade)) return false
        if (minDewPointTemperatureCentigrade != other.minDewPointTemperatureCentigrade) return false
        if (maxDewPointTemperatureCentigrade != other.maxDewPointTemperatureCentigrade) return false
        if (avgDewPointTemperatureCentigrade != other.avgDewPointTemperatureCentigrade) return false
        if (!hourlyHumidityPercent.contentEquals(other.hourlyHumidityPercent)) return false
        if (minHumidityPercent != other.minHumidityPercent) return false
        if (maxHumidityPercent != other.maxHumidityPercent) return false
        if (avgHumidityPercent != other.avgHumidityPercent) return false
        if (!hourlyAirPressureHectopascals.contentEquals(other.hourlyAirPressureHectopascals)) return false
        if (minAirPressureHectopascals != other.minAirPressureHectopascals) return false
        if (avgAirPressureHectopascals != other.avgAirPressureHectopascals) return false
        if (maxAirPressureHectopascals != other.maxAirPressureHectopascals) return false
        if (!hourlyCloudCoverages.contentEquals(other.hourlyCloudCoverages)) return false
        if (!hourlySunshineDurationMinutes.contentEquals(other.hourlySunshineDurationMinutes)) return false
        if (sumSunshineDurationHours != other.sumSunshineDurationHours) return false
        if (!hourlyRainfallMillimeters.contentEquals(other.hourlyRainfallMillimeters)) return false
        if (sumRainfallMillimeters != other.sumRainfallMillimeters) return false
        if (!hourlySnowfallMillimeters.contentEquals(other.hourlySnowfallMillimeters)) return false
        if (sumSnowfallMillimeters != other.sumSnowfallMillimeters) return false
        if (!hourlyWindSpeedMetersPerSecond.contentEquals(other.hourlyWindSpeedMetersPerSecond)) return false
        if (maxWindSpeedMetersPerSecond != other.maxWindSpeedMetersPerSecond) return false
        if (avgWindSpeedMetersPerSecond != other.avgWindSpeedMetersPerSecond) return false
        if (!hourlyWindDirectionDegrees.contentEquals(other.hourlyWindDirectionDegrees)) return false
        if (!hourlyVisibilityMeters.contentEquals(other.hourlyVisibilityMeters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = day.hashCode()
        result = 31 * result + hourlyAirTemperatureCentigrade.contentHashCode()
        result = 31 * result + (minAirTemperatureCentigrade?.hashCode() ?: 0)
        result = 31 * result + (avgAirTemperatureCentigrade?.hashCode() ?: 0)
        result = 31 * result + (maxAirTemperatureCentigrade?.hashCode() ?: 0)
        result = 31 * result + hourlyDewPointTemperatureCentigrade.contentHashCode()
        result = 31 * result + (minDewPointTemperatureCentigrade?.hashCode() ?: 0)
        result = 31 * result + (maxDewPointTemperatureCentigrade?.hashCode() ?: 0)
        result = 31 * result + (avgDewPointTemperatureCentigrade?.hashCode() ?: 0)
        result = 31 * result + hourlyHumidityPercent.contentHashCode()
        result = 31 * result + (minHumidityPercent?.hashCode() ?: 0)
        result = 31 * result + (maxHumidityPercent?.hashCode() ?: 0)
        result = 31 * result + (avgHumidityPercent?.hashCode() ?: 0)
        result = 31 * result + hourlyAirPressureHectopascals.contentHashCode()
        result = 31 * result + (minAirPressureHectopascals?.hashCode() ?: 0)
        result = 31 * result + (avgAirPressureHectopascals?.hashCode() ?: 0)
        result = 31 * result + (maxAirPressureHectopascals?.hashCode() ?: 0)
        result = 31 * result + hourlyCloudCoverages.contentHashCode()
        result = 31 * result + hourlySunshineDurationMinutes.contentHashCode()
        result = 31 * result + (sumSunshineDurationHours?.hashCode() ?: 0)
        result = 31 * result + hourlyRainfallMillimeters.contentHashCode()
        result = 31 * result + (sumRainfallMillimeters?.hashCode() ?: 0)
        result = 31 * result + hourlySnowfallMillimeters.contentHashCode()
        result = 31 * result + (sumSnowfallMillimeters?.hashCode() ?: 0)
        result = 31 * result + hourlyWindSpeedMetersPerSecond.contentHashCode()
        result = 31 * result + (maxWindSpeedMetersPerSecond?.hashCode() ?: 0)
        result = 31 * result + (avgWindSpeedMetersPerSecond?.hashCode() ?: 0)
        result = 31 * result + hourlyWindDirectionDegrees.contentHashCode()
        result = 31 * result + hourlyVisibilityMeters.contentHashCode()
        return result
    }
}

private fun hourList(): MutableList<Int?> {
    val list = ArrayList<Int?>(24)
    list.fill(null)
    return list
}
