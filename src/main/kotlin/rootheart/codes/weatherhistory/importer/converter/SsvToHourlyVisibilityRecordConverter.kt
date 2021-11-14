package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyVisibilityRecord

object SsvToHourlyVisibilityRecordConverter : RecordConverter<HourlyVisibilityRecord>(
    ::HourlyVisibilityRecord,
    mapOf(
        "QN_8" to QualityLevelProperty(HourlyVisibilityRecord::qualityLevel),
        "V_VV_I" to MeasurementOrObservationProperty(HourlyVisibilityRecord::measurementOrObservation),
        "V_VV" to BigDecimalProperty(HourlyVisibilityRecord::visibilityInMeters)
    )
)