package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.StationId
import java.time.LocalDateTime

abstract class BaseRecord {
    var stationId: StationId? = null
    var measurementTime: LocalDateTime? = null
    var qualityLevel: QualityLevel? = null
}