package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlyMaxWindSpeedRecord(var maxWindSpeedMetersPerSecond: BigDecimal? = null) : BaseRecord()