package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyAirTemperatureRecordConverter
import rootheart.codes.weatherhistory.importer.records.HourlyAirTemperatureRecord
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.util.stream.Collectors

class SsvToHourlyCloudTypeRecordConverterSpec extends Specification {

    @Unroll
    def 'Test that strings are converted correctly to hourly cloud type record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly cloud type records'
        def converter = new SsvToHourlyAirTemperatureRecordConverter()

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(
                SsvToHourlyCloudTypeRecordConverter.EXPECTED_COLUMN_NAMES,
                values.stream()
        )

        when: 'the input is converted'
        def records = converter.convert(ssvData)

        then: 'each input line has a corresponding air temperature record'
        records.collect(Collectors.toList()) == expectedRecords

        where:
        values                                                | expectedRecords
        [['1', '2021110901', '1', '19.3', '70.5', 'eor']]     | [new HourlyAirTemperatureRecord(StationId.of(1), LocalDateTime.of(2021, 11, 9, 1, 0), QualityLevel.ONE, 19.3, 70.5)]
        [['4711', '2001010100', '8', '-3.6', '-70.5', 'eor']] | [new HourlyAirTemperatureRecord(StationId.of(4711), LocalDateTime.of(2001, 1, 1, 0, 0), QualityLevel.EIGHT, -3.6, -70.5)]
    }
}
