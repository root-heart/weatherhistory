package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlySoilTemperatureRecord

object SsvToHourlySoilTemperatureRecordConverter : RecordConverter<HourlySoilTemperatureRecord>(
    ::HourlySoilTemperatureRecord,
    mapOf(
        "QN_2" to QualityLevelProperty(HourlySoilTemperatureRecord::qualityLevel),
        "V_TE002" to BigDecimalProperty(HourlySoilTemperatureRecord::soilTemperature2Centimeters),
        "V_TE005" to BigDecimalProperty(HourlySoilTemperatureRecord::soilTemperature5Centimeters),
        "V_TE010" to BigDecimalProperty(HourlySoilTemperatureRecord::soilTemperature10Centimeters),
        "V_TE020" to BigDecimalProperty(HourlySoilTemperatureRecord::soilTemperature20Centimeters),
        "V_TE050" to BigDecimalProperty(HourlySoilTemperatureRecord::soilTemperature50Centimeters),
        "V_TE100" to BigDecimalProperty(HourlySoilTemperatureRecord::soilTemperature100Centimeters),
    )
)