package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.MeasurementOrObservation
import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.StationId
import java.math.BigDecimal
import java.time.LocalDateTime

data class HourlyVisibilityRecord(
    val stationId: StationId,
    val measurementTime: LocalDateTime,
    val qualityLevel: QualityLevel,
    val visibilityMeasurementOrObservation: MeasurementOrObservation,
    val visibilityInMeters: BigDecimal
)
