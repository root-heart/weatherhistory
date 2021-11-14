package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.model.QualityLevel
import rootheart.codes.weatherhistory.model.StationId
import java.time.LocalDateTime

abstract class BaseRecord {
    var stationId: StationId? = null
    var measurementTime: LocalDateTime? = null
    var qualityLevel: QualityLevel? = null
}