package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyMaxWindSpeedRecord

object HourlyMaxWindSpeedRecordConverter : RecordConverter<HourlyMaxWindSpeedRecord>(
    ::HourlyMaxWindSpeedRecord,
    mapOf(
        "QN_8" to QualityLevelProperty(HourlyMaxWindSpeedRecord::qualityLevel),
        "FX_911" to BigDecimalProperty(HourlyMaxWindSpeedRecord::maxWindSpeedMetersPerSecond)
    )
)