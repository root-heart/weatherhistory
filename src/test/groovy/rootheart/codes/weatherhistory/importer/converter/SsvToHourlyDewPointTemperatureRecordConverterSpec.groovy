package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.QualityLevel
import rootheart.codes.weatherhistory.importer.SsvData
import rootheart.codes.weatherhistory.importer.StationId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class SsvToHourlyDewPointTemperatureRecordConverterSpec extends Specification {
    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly dew point temperature record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly dew point temperature records'
        def converter = SsvToHourlyDewPointTemperatureRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'the values of each record matches the parsed values of each input line'
        records*.stationId == values.collect { it[0] != null ? StationId.of(it[0]) : null }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { it[2] != null ? QualityLevel.of(it[2]) : null }
        records*.dewPointTemperatureCentigrade == values.collect { it[3] != null ? new BigDecimal(it[3]) : null }

        where:
        columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_8", "TT"]

        and:
        description                          | values
        'no records'                         | []
        'one record'                         | [['1', '2021110901', '1', '19.3']]
        'one record, null quality level'     | [['1', '2021110901', null, '19.3']]
        'one record, null measurement value' | [['1', '2021110901', '1', null]]
        'two records'                        | [['4711', '2001010100', '8', '-3.6'], ['4711', '2020020200', '5', '7.6']]
    }
}
