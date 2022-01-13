package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlyDewPointTemperatureRecord(
    var dewPointTemperatureCentigrade: BigDecimal? = null,
    var airTemperatureCentigrade: BigDecimal? = null
) : BaseRecord()
