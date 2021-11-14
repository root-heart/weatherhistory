package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.model.MeasurementOrObservation
import java.math.BigDecimal

data class HourlyVisibilityRecord(
    var measurementOrObservation: MeasurementOrObservation? = null,
    var visibilityInMeters: BigDecimal? = null
) : BaseRecord()
