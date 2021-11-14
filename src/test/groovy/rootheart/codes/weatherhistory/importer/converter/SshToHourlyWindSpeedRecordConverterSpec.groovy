package rootheart.codes.weatherhistory.importer.converter


import rootheart.codes.weatherhistory.importer.SpecUtils
import rootheart.codes.weatherhistory.importer.SsvData
import rootheart.codes.weatherhistory.importer.StationId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class SshToHourlyWindSpeedRecordConverterSpec extends Specification implements SpecUtils {
    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly wind speed record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly wind speed temperature records'
        def converter = SsvToHourlyWindSpeedRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'the values of each record matches the parsed values of each input line'
        records*.stationId == values.collect { it[0] != null ? StationId.of(it[0]) : null }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == allQualityLevelsOf(values, 2)
        records*.windSpeedMetersPerSecond == allBigDecimalsOf(values, 3)
        records*.windDirectionDegrees == allBigDecimalsOf(values, 4)

        where:
        columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_3", "F", "D"]

        and:
        description                       | values
        'no records'                      | []
        'one record'                      | [['1', '2021110901', '1', '19.3', '271.5']]
        'one record, null quality level'  | [['1', '2021110901', null, '19.3', '271.5']]
        'one record, null wind speed'     | [['1', '2021110901', '1', null, '271.5']]
        'one record, null wind direction' | [['1', '2021110901', '1', '19.3', null]]
    }
}
