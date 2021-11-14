package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.model.PrecipitationType
import java.math.BigDecimal

data class HourlyPrecipitationRecord(
    var precipitationMillimeters: BigDecimal? = null,
    var precipitationType: PrecipitationType? = null
) : BaseRecord()
