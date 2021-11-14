package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.MeasurementOrObservation
import java.math.BigDecimal

data class HourlyVisibilityRecord(
    var measurementOrObservation: MeasurementOrObservation? = null,
    var visibilityInMeters: BigDecimal? = null
) : BaseRecord()
