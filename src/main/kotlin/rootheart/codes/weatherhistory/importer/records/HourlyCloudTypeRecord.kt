package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.CloudLayer
import rootheart.codes.weatherhistory.importer.MeasurementOrObservation

data class HourlyCloudTypeRecord(
    var overallCoverage: Int? = null,
    var measurementOrObservation: MeasurementOrObservation? = null,
    var layer1: CloudLayer? = null,
    var layer2: CloudLayer? = null,
    var layer3: CloudLayer? = null,
    var layer4: CloudLayer? = null
) : BaseRecord()