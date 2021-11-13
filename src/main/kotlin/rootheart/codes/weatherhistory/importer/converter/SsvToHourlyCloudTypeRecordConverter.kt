package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyCloudTypeRecord

object SsvToHourlyCloudTypeRecordConverter : RecordConverter<HourlyCloudTypeRecord>(
    ::HourlyCloudTypeRecord,
    mapOf(
    )
)