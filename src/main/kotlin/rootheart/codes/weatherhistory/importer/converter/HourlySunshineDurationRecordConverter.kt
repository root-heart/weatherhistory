package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlySunshineDurationRecord

object HourlySunshineDurationRecordConverter : RecordConverter<HourlySunshineDurationRecord>(
    ::HourlySunshineDurationRecord,
    mapOf(
        "QN_7" to QualityLevelProperty(HourlySunshineDurationRecord::qualityLevel),
        "SD_SO" to BigDecimalProperty(HourlySunshineDurationRecord::sunshineDuration)
    )
)