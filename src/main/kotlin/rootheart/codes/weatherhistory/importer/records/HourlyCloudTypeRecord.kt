package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.CloudLayer
import rootheart.codes.weatherhistory.importer.MeasurementOrObservation
import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.StationId
import java.time.LocalDateTime

data class HourlyCloudTypeRecord(
    var overallCoverage: Int? = null,
    var measurementOrObservation: MeasurementOrObservation? = null,
    var layer1: CloudLayer? = null
) : BaseRecord()