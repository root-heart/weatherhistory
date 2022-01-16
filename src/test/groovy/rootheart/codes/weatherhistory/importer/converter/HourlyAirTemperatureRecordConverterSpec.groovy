package rootheart.codes.weatherhistory.importer.converter


import rootheart.codes.weatherhistory.importer.SpecUtils
import rootheart.codes.weatherhistory.importer.ssv.SsvData
import rootheart.codes.weatherhistory.model.QualityLevel
import rootheart.codes.weatherhistory.model.StationId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class HourlyAirTemperatureRecordConverterSpec extends Specification implements SpecUtils {

    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly air temperature record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly air temperature records'
        def converter = HourlyAirTemperatureRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'the values of each record matches the parsed values of each input line'
        records*.stationId == values.collect { StationId.of(it[0]) }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == allQualityLevelsOf(values, 2)
        records*.airTemperatureAtTwoMetersHeightCentigrade == allBigDecimalsOf(values, 3)
        records*.relativeHumidityPercent == allBigDecimalsOf(values, 4)

        where:
        columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_9", "TT_TU", "RF_TU"]

        and:
        description   | values
        'no records'  | []
        'one record'  | [['1', '2021110901', '1', '19.3', '70.5']]
        'two records' | [['4711', '2001010100', '8', '-3.6', '-70.5'], ['4711', '2020020200', '5', '7.6', '12.3']]
    }

    def 'Test that an error is thrown if the column names are not as expected'() {
        given: 'a converter able to convert from semicolon-separated data to hourly air temperature records'
        def converter = HourlyAirTemperatureRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, [].stream())

        when: 'the input is converted'
        converter.convert(ssvData).collect(Collectors.toList())

        then:
        thrown expectedException

        where:
        columnNames                                                                                | expectedException
        ["STATIONS_ID", "MESS_DATUM", "QN_9", "TT_TU", "RF_TU", "one column that is not expected"] | InvalidColumnsException
        ["STATIONS_ID", "MESS_DATUM", "QN_9", "TT_TU", "RF_TU is missing here"]                    | InvalidColumnsException
        []                                                                                         | InvalidColumnsException
//        null                                                                                       | NullPointerException
    }
}
