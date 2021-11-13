package rootheart.codes.weatherhistory.importer


import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyCloudTypeRecordConverter
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class SsvToHourlyCloudTypeRecordConverterSpec extends Specification {
    static final columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_8", "V_N", "V_N_I",
                                "V_S1_CS", "V_S1_CSA", "V_S1_HHS", "V_S1_NS",
                                "V_S2_CS", "V_S2_CSA", "V_S2_HHS", "V_S2_NS",
                                "V_S3_CS", "V_S3_CSA", "V_S3_HHS", "V_S3_NS",
                                "V_S4_CS", "V_S4_CSA", "V_S4_HHS", "V_S4_NS"]

    static final String NULL_STRING = "-999"

    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly cloud type record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'each input line has a corresponding cloud type record'
        records*.stationId == values.collect { StationId.of(it[0]) }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { QualityLevel.of(it[2]) }
        records*.overallCoverage == values.collect { Integer.parseInt(it[3]) }
        records*.measurementOrObservation == values.collect { MeasurementOrObservation.of(it[4]) }

        and: 'the four different layers are not set'
        records.every { it.layer1 == null }
        records.every { it.layer2 == null }
        records.every { it.layer3 == null }
        records.every { it.layer4 == null }

        where:
        description          | values
        'all fields set'     | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'station id not set' | [['-999', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 1'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'each input line has a corresponding cloud type record'
        records*.stationId == values.collect { StationId.of(it[0]) }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { QualityLevel.of(it[2]) }
        records*.overallCoverage == values.collect { Integer.parseInt(it[3]) }
        records*.measurementOrObservation == values.collect { MeasurementOrObservation.of(it[4]) }

        and: 'only the first layer is set'
        records.every { it.layer1 != null }
        records.every { it.layer2 == null }
        records.every { it.layer3 == null }
        records.every { it.layer4 == null }

        and: 'the values in the first layer are set correctly'
        records*.layer1.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[5], it[6]) }
        records*.layer1.collect { it.height } == values.collect { it[7] == '-999' ? null : Integer.parseInt(it[7]) }
        records*.layer1.collect { it.coverage } == values.collect { it[8] == '-999' ? null : Integer.parseInt(it[8]) }

        where:
        description                                     | values
        'one record, cloud type code on layer1'         | [['44', '1979010100', '1', '2', 'P', '1', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, cloud type abbreviation on layer1' | [['44', '1979010100', '1', '2', 'P', '-999', 'CI', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, cloud height on layer1'            | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '1234', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, coverage on layer1'                | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '5', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 2'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'each input line has a corresponding cloud type record'
        records*.stationId == values.collect { StationId.of(it[0]) }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { QualityLevel.of(it[2]) }
        records*.overallCoverage == values.collect { Integer.parseInt(it[3]) }
        records*.measurementOrObservation == values.collect { MeasurementOrObservation.of(it[4]) }

        and: 'only the second layer is set'
        records.every { it.layer1 == null }
        records.every { it.layer2 != null }
        records.every { it.layer3 == null }
        records.every { it.layer4 == null }

        and: 'the values in the second layer are set correctly'
        records*.layer2.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[9], it[10]) }
        records*.layer2.collect { it.height } == values.collect { it[11] == '-999' ? null : Integer.parseInt(it[11]) }
        records*.layer2.collect { it.coverage } == values.collect { it[12] == '-999' ? null : Integer.parseInt(it[12]) }

        where:
        description                                     | values
        'one record, cloud type code on layer2'         | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '1', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, cloud type abbreviation on layer2' | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', 'CI', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, cloud height on layer2'            | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '1234', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, coverage on layer2'                | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '5', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]

    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 3'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'each input line has a corresponding cloud type record'
        records*.stationId == values.collect { StationId.of(it[0]) }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { QualityLevel.of(it[2]) }
        records*.overallCoverage == values.collect { Integer.parseInt(it[3]) }
        records*.measurementOrObservation == values.collect { MeasurementOrObservation.of(it[4]) }

        and: 'only the third layer is set'
        records.every { it.layer1 == null }
        records.every { it.layer2 == null }
        records.every { it.layer3 != null }
        records.every { it.layer4 == null }

        and: 'the values in the third layer are set correctly'
        records*.layer3.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[13], it[14]) }
        records*.layer3.collect { it.height } == values.collect { it[15] == '-999' ? null : Integer.parseInt(it[15]) }
        records*.layer3.collect { it.coverage } == values.collect { it[16] == '-999' ? null : Integer.parseInt(it[16]) }

        where:
        description                                     | values
        'one record, cloud type code on layer3'         | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '1', '-999', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, cloud type abbreviation on layer3' | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', 'CI', '-999', '-999', '-999', '-999', '-999', '-999']]
        'one record, cloud height on layer3'            | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '1234', '-999', '-999', '-999', '-999', '-999']]
        'one record, coverage on layer3'                | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '5', '-999', '-999', '-999', '-999']]
    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 4'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'each input line has a corresponding cloud type record'
        records*.stationId == values.collect { StationId.of(it[0]) }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { QualityLevel.of(it[2]) }
        records*.overallCoverage == values.collect { Integer.parseInt(it[3]) }
        records*.measurementOrObservation == values.collect { MeasurementOrObservation.of(it[4]) }

        and: 'only the fourth layer is set'
        records.every { it.layer1 == null }
        records.every { it.layer2 == null }
        records.every { it.layer3 == null }
        records.every { it.layer4 != null }

        and: 'the values in the fourth layer are set correctly'
        records*.layer4.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[17], it[18]) }
        records*.layer4.collect { it.height } == values.collect { it[19] == '-999' ? null : Integer.parseInt(it[19]) }
        records*.layer4.collect { it.coverage } == values.collect { it[20] == '-999' ? null : Integer.parseInt(it[20]) }

        where:
        description                                     | values
        'one record, cloud type code on layer4'         | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '1', '-999', '-999', '-999']]
        'one record, cloud type abbreviation on layer4' | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', 'CI', '-999', '-999']]
        'one record, cloud height on layer4'            | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '1234', '-999']]
        'one record, coverage on layer4'                | [['44', '1979010100', '1', '2', 'P', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '-999', '5']]
    }

    static CloudType cloudTypeOfCodeAndAbbreviation(String code, String abbreviation) {
        if (code != NULL_STRING && abbreviation != NULL_STRING) {
            CloudType byCode = CloudType.of(code)
            CloudType byAbbreviation = CloudType.ofAbbreviation(abbreviation)
            if (byCode != byAbbreviation) {
                return null
            } else {
                return byCode
            }
        } else if (code != NULL_STRING) {
            return CloudType.of(code)
        } else if (abbreviation != NULL_STRING) {
            return CloudType.ofAbbreviation(abbreviation)
        }
        return null
    }
}
