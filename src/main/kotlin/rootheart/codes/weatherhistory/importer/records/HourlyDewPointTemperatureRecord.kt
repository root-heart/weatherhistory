package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlyDewPointTemperatureRecord(var dewPointTemperatureCentigrade: BigDecimal? = null) : BaseRecord()
