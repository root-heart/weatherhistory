package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.HourlyCloudTypeRecord

object SsvToHourlyCloudTypeRecordConverter : RecordConverter<HourlyCloudTypeRecord>(
    ::HourlyCloudTypeRecord,
    mapOf(
        "QN_8" to QualityLevelProperty(HourlyCloudTypeRecord::qualityLevel),
        "V_N" to IntProperty(HourlyCloudTypeRecord::overallCoverage),
        "V_N_I" to MeasurementOrObservationProperty(HourlyCloudTypeRecord::measurementOrObservation),
        "V_S1_CS" to CloudLayerCloudTypeProperty(HourlyCloudTypeRecord::layer1),
        "V_S1_CSA" to CloudLayerCloudTypeAbbreviationProperty(HourlyCloudTypeRecord::layer1),
        "V_S1_HHS" to CloudLayerHeightProperty(HourlyCloudTypeRecord::layer1),
        "V_S1_NS" to CloudLayerCoverageProperty(HourlyCloudTypeRecord::layer1),
        "V_S2_CS" to CloudLayerCloudTypeProperty(HourlyCloudTypeRecord::layer2),
        "V_S2_CSA" to CloudLayerCloudTypeAbbreviationProperty(HourlyCloudTypeRecord::layer2),
        "V_S2_HHS" to CloudLayerHeightProperty(HourlyCloudTypeRecord::layer2),
        "V_S2_NS" to CloudLayerCoverageProperty(HourlyCloudTypeRecord::layer2),
        "V_S3_CS" to CloudLayerCloudTypeProperty(HourlyCloudTypeRecord::layer3),
        "V_S3_CSA" to CloudLayerCloudTypeAbbreviationProperty(HourlyCloudTypeRecord::layer3),
        "V_S3_HHS" to CloudLayerHeightProperty(HourlyCloudTypeRecord::layer3),
        "V_S3_NS" to CloudLayerCoverageProperty(HourlyCloudTypeRecord::layer3),
        "V_S4_CS" to CloudLayerCloudTypeProperty(HourlyCloudTypeRecord::layer4),
        "V_S4_CSA" to CloudLayerCloudTypeAbbreviationProperty(HourlyCloudTypeRecord::layer4),
        "V_S4_HHS" to CloudLayerHeightProperty(HourlyCloudTypeRecord::layer4),
        "V_S4_NS" to CloudLayerCoverageProperty(HourlyCloudTypeRecord::layer4),
    )
)