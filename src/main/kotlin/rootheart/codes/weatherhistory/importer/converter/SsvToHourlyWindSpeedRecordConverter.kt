package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyWindSpeedRecord

object SsvToHourlyWindSpeedRecordConverter : RecordConverter<HourlyWindSpeedRecord>(
    ::HourlyWindSpeedRecord,
    mapOf(
        "QN_3" to QualityLevelProperty(HourlyWindSpeedRecord::qualityLevel),
        "F" to BigDecimalProperty(HourlyWindSpeedRecord::windSpeedMetersPerSecond),
        "D" to BigDecimalProperty(HourlyWindSpeedRecord::windDirectionDegrees)
    )
)