package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyDewPointTemperatureRecord

object HourlyDewPointTemperatureRecordConverter : RecordConverter<HourlyDewPointTemperatureRecord>(
    ::HourlyDewPointTemperatureRecord,
    mapOf(
        "QN_8" to QualityLevelProperty(HourlyDewPointTemperatureRecord::qualityLevel),
        "TD" to BigDecimalProperty(HourlyDewPointTemperatureRecord::dewPointTemperatureCentigrade),
        "TT" to BigDecimalProperty(HourlyDewPointTemperatureRecord::airTemperatureCentigrade)
    )
)
