package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.StationId
import java.math.BigDecimal
import java.time.LocalDateTime

data class HourlySunshineDurationRecord(
    val stationId: StationId,
    val measurementTime: LocalDateTime,
    val qualityLevel: QualityLevel,
    val sunshineDuration: BigDecimal
)
