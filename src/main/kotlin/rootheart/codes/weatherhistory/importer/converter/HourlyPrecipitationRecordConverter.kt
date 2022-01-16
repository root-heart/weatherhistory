package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyPrecipitationRecord

object HourlyPrecipitationRecordConverter : RecordConverter<HourlyPrecipitationRecord>(
    ::HourlyPrecipitationRecord,
    mapOf(
        "QN_8" to QualityLevelProperty(HourlyPrecipitationRecord::qualityLevel),
        "R1" to BigDecimalProperty(HourlyPrecipitationRecord::precipitationMillimeters),
        "RS_IND" to RecordProperty { _, _ -> },
        "WRTR" to PrecipitationTypeProperty(HourlyPrecipitationRecord::precipitationType)
    )
)



