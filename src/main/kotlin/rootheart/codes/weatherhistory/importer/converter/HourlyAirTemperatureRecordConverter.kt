package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyAirTemperatureRecord

object HourlyAirTemperatureRecordConverter : RecordConverter<HourlyAirTemperatureRecord>(
    ::HourlyAirTemperatureRecord,
    mapOf(
        "QN_9" to QualityLevelProperty(HourlyAirTemperatureRecord::qualityLevel),
        "TT_TU" to BigDecimalProperty(HourlyAirTemperatureRecord::airTemperatureAtTwoMetersHeightCentigrade),
        "RF_TU" to BigDecimalProperty(HourlyAirTemperatureRecord::relativeHumidityPercent)
    )
)