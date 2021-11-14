package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlySunshineDurationRecord(var sunshineDuration: BigDecimal? = null) : BaseRecord()
