package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlyAirTemperatureRecord(
    var airTemperatureAtTwoMetersHeightCentigrade: BigDecimal? = null,
    var relativeHumidityPercent: BigDecimal? = null
) : BaseRecord()
