package rootheart.codes.weatherhistory.importer.records

import java.math.BigDecimal

data class HourlySoilTemperatureRecord(
    var soilTemperature2Centimeters: BigDecimal? = null,
    var soilTemperature5Centimeters: BigDecimal? = null,
    var soilTemperature10Centimeters: BigDecimal? = null,
    var soilTemperature20Centimeters: BigDecimal? = null,
    var soilTemperature50Centimeters: BigDecimal? = null,
    var soilTemperature100Centimeters: BigDecimal? = null,
) : BaseRecord()