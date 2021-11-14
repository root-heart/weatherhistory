package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.CloudType
import rootheart.codes.weatherhistory.importer.MeasurementOrObservation
import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.SpecUtils
import rootheart.codes.weatherhistory.importer.SsvData
import rootheart.codes.weatherhistory.importer.StationId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class SsvToHourlyCloudTypeRecordConverterSpec extends Specification implements SpecUtils {
    static final columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_8", "V_N", "V_N_I",
                                "V_S1_CS", "V_S1_CSA", "V_S1_HHS", "V_S1_NS",
                                "V_S2_CS", "V_S2_CSA", "V_S2_HHS", "V_S2_NS",
                                "V_S3_CS", "V_S3_CSA", "V_S3_HHS", "V_S3_NS",
                                "V_S4_CS", "V_S4_CSA", "V_S4_HHS", "V_S4_NS"]


    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly cloud type record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'each input line has a corresponding cloud type record'
        records*.stationId == values.collect { it[0] != null ? StationId.of(it[0]) : null }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { QualityLevel.of(it[2]) }
        records*.overallCoverage == allIntsOf(values, 3)
        records*.measurementOrObservation == values.collect { MeasurementOrObservation.of(it[4]) }

        and: 'the four different layers are not set'
        records.every { it.layer1 == null }
        records.every { it.layer2 == null }
        records.every { it.layer3 == null }
        records.every { it.layer4 == null }

        where:
        description      | values
        'all fields set' | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null]]
    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 1'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'only the first layer is set'
        records.every { it.layer1 != null }
        records.every { it.layer2 == null }
        records.every { it.layer3 == null }
        records.every { it.layer4 == null }

        and: 'the values in the first layer are set correctly'
        records*.layer1.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[5], it[6]) }
        records*.layer1.collect { it.height } == allIntsOf(values, 7)
        records*.layer1.collect { it.coverage } == allIntsOf(values, 8)

        where:
        description                                     | values
        'one record, cloud type code on layer1'         | [['44', '1979010100', '1', '2', 'P', '1', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null]]
        'one record, cloud type abbreviation on layer1' | [['44', '1979010100', '1', '2', 'P', null, 'CI', null, null, null, null, null, null, null, null, null, null, null, null, null, null]]
        'one record, cloud height on layer1'            | [['44', '1979010100', '1', '2', 'P', null, null, '1234', null, null, null, null, null, null, null, null, null, null, null, null, null]]
        'one record, coverage on layer1'                | [['44', '1979010100', '1', '2', 'P', null, null, null, '5', null, null, null, null, null, null, null, null, null, null, null, null]]
    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 2'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'only the second layer is set'
        records.every { it.layer1 == null }
        records.every { it.layer2 != null }
        records.every { it.layer3 == null }
        records.every { it.layer4 == null }

        and: 'the values in the second layer are set correctly'
        records*.layer2.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[9], it[10]) }
        records*.layer2.collect { it.height } == allIntsOf(values, 11)
        records*.layer2.collect { it.coverage } == allIntsOf(values, 12)

        where:
        description                                     | values
        'one record, cloud type code on layer2'         | [['44', '1979010100', '1', '2', 'P', null, null, null, null, '1', null, null, null, null, null, null, null, null, null, null, null]]
        'one record, cloud type abbreviation on layer2' | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, 'CI', null, null, null, null, null, null, null, null, null, null]]
        'one record, cloud height on layer2'            | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, '1234', null, null, null, null, null, null, null, null, null]]
        'one record, coverage on layer2'                | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, '5', null, null, null, null, null, null, null, null]]

    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 3'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'only the third layer is set'
        records.every { it.layer1 == null }
        records.every { it.layer2 == null }
        records.every { it.layer3 != null }
        records.every { it.layer4 == null }

        and: 'the values in the third layer are set correctly'
        records*.layer3.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[13], it[14]) }
        records*.layer3.collect { it.height } == allIntsOf(values, 15)
        records*.layer3.collect { it.coverage } == allIntsOf(values, 16)

        where:
        description                                     | values
        'one record, cloud type code on layer3'         | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, '1', null, null, null, null, null, null, null]]
        'one record, cloud type abbreviation on layer3' | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, 'CI', null, null, null, null, null, null]]
        'one record, cloud height on layer3'            | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, null, '1234', null, null, null, null, null]]
        'one record, coverage on layer3'                | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, null, null, '5', null, null, null, null]]
    }

    @Unroll('#description')
    def 'Test that the values are correctly set in cloud layer 4'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = SsvToHourlyCloudTypeRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'only the fourth layer is set'
        records.every { it.layer1 == null }
        records.every { it.layer2 == null }
        records.every { it.layer3 == null }
        records.every { it.layer4 != null }

        and: 'the values in the fourth layer are set correctly'
        records*.layer4.collect { it.cloudType } == values.collect { cloudTypeOfCodeAndAbbreviation(it[17], it[18]) }
        records*.layer4.collect { it.height } == allIntsOf(values, 19)
        records*.layer4.collect { it.coverage } == allIntsOf(values, 20)

        where:
        description                                     | values
        'one record, cloud type code on layer4'         | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, null, null, null, '1', null, null, null]]
        'one record, cloud type abbreviation on layer4' | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, null, null, null, null, 'CI', null, null]]
        'one record, cloud height on layer4'            | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, null, null, null, null, null, '1234', null]]
        'one record, coverage on layer4'                | [['44', '1979010100', '1', '2', 'P', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, '5']]
    }

    static CloudType cloudTypeOfCodeAndAbbreviation(String code, String abbreviation) {
        if (code != null && abbreviation != null) {
            CloudType byCode = CloudType.of(code)
            CloudType byAbbreviation = CloudType.ofAbbreviation(abbreviation)
            if (byCode != byAbbreviation) {
                return null
            } else {
                return byCode
            }
        } else if (code != null) {
            return CloudType.of(code)
        } else if (abbreviation != null) {
            return CloudType.ofAbbreviation(abbreviation)
        }
        return null
    }
}
