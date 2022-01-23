package rootheart.codes.weatherhistory.importer

import org.joda.time.LocalDateTime
import rootheart.codes.weatherhistory.model.PrecipitationType
import java.math.BigDecimal

data class HourlyMeasurement(
    val measurementTime: LocalDateTime,
    var airTemperatureAtTwoMetersHeightCentigrade: BigDecimal? = null,
    var relativeHumidityPercent: BigDecimal? = null,
    var cloudCoverage: Int? = null,
    var dewPointTemperatureCentigrade: BigDecimal? = null,
    var airPressureHectopascals: BigDecimal? = null,
    var precipitationMillimeters: BigDecimal? = null,
    var precipitationType: PrecipitationType? = null,
    var sunshineDuration: BigDecimal? = null,
    var windSpeedMetersPerSecond: BigDecimal? = null,
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var windDirectionDegrees: BigDecimal? = null,
    var visibilityInMeters: BigDecimal? = null
)

typealias HourlyMeasurements = Collection<HourlyMeasurement>

data class DailyMeasurement(
    val measurementTime: LocalDateTime,
    val minAirTemperature: BigDecimal? = null,
    val avgAirTemperature: BigDecimal? = null,
    val maxAirTemperature: BigDecimal? = null,
)