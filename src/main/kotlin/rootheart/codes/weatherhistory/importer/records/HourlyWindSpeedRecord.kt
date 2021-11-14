package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlyWindSpeedRecord(
    var windSpeedMetersPerSecond: BigDecimal? = null,
    var windDirectionDegrees: BigDecimal? = null
) : BaseRecord()
