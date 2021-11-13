package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyDewPointTemperatureRecord

object SsvToHourlyDewPointTemperatureRecordConverter : RecordConverter<HourlyDewPointTemperatureRecord>(
    ::HourlyDewPointTemperatureRecord,
    mapOf(
        "QN_8" to QualityLevelProperty(HourlyDewPointTemperatureRecord::qualityLevel),
        "TT" to BigDecimalProperty(HourlyDewPointTemperatureRecord::dewPointTemperatureCentigrade)
    )
)
