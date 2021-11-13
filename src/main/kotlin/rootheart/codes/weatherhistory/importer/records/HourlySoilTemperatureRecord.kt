package rootheart.codes.weatherhistory.importer.records

import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.StationId
import java.math.BigDecimal
import java.time.LocalDateTime

data class HourlySoilTemperatureRecord(
    val stationId: StationId,
    val measurementTime: LocalDateTime,
    val qualityLevel: QualityLevel,
    val soilTemperature2Centimeters: BigDecimal,
    val soilTemperature5Centimeters: BigDecimal,
    val soilTemperature10Centimeters: BigDecimal,
    val soilTemperature20Centimeters: BigDecimal,
    val soilTemperature50Centimeters: BigDecimal,
    val soilTemperature100Centimeters: BigDecimal,
)
