package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.model.PrecipitationType
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal
import java.time.LocalDateTime

data class HourlyRecord(
    val stationId: StationId,
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