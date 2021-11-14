package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.PrecipitationType
import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.StationId
import java.math.BigDecimal
import java.time.LocalDateTime

data class HourlyPrecipitationRecord(
    var precipitationMillimeters: BigDecimal? = null,
    var precipitationType: PrecipitationType? = null
) : BaseRecord()
