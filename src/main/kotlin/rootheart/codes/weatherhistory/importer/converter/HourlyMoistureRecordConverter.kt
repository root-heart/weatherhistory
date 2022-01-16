package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyMoistureRecord

object HourlyMoistureRecordConverter : RecordConverter<HourlyMoistureRecord>(
    ::HourlyMoistureRecord,
    mapOf(
        "QN_8" to QualityLevelProperty(HourlyMoistureRecord::qualityLevel),
        "ABSF_STD" to BigDecimalProperty(HourlyMoistureRecord::absoluteHumidity),
        "P_STD" to BigDecimalProperty(HourlyMoistureRecord::airPressureHectopascals),
        "RF_STD" to BigDecimalProperty(HourlyMoistureRecord::relativeHumidityPercent),
        "TD_STD" to BigDecimalProperty(HourlyMoistureRecord::dewPointTemperatureCentigrade),
        "TF_STD" to BigDecimalProperty(HourlyMoistureRecord::wetBulbTemperatureCentigrade),
        "VP_STD" to BigDecimalProperty(HourlyMoistureRecord::vaporPressureHectopascals),
        "TT_STD" to BigDecimalProperty(HourlyMoistureRecord::airTemperatureAtTwoMetersHeightCentigrade)
    )
)