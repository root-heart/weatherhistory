package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.model.QualityLevel
import rootheart.codes.weatherhistory.importer.SpecUtils
import rootheart.codes.weatherhistory.importer.ssv.SemicolonSeparatedValues
import rootheart.codes.weatherhistory.model.StationId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class HourlyPrecipitationRecordConverterSpec extends Specification implements SpecUtils {
    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly precipitation record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly precipitation records'
        def converter = HourlyPrecipitationRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SemicolonSeparatedValues(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'the values of each record matches the parsed values of each input line'
        records*.stationId == values.collect { it[0] != null ? StationId.of(it[0]) : null }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { it[2] != null ? QualityLevel.of(it[2]) : null }
        records*.precipitationMillimeters == allBigDecimalsOf(values, 3)
        records*.precipitationType == allPrecipitationTypesOf(values, 5)

        where:
        columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_8", "R1", "RS_IND", "WRTR"]

        and:
        description                           | values
        'no records'                          | []
        'one record'                          | [['1', '2021110901', '1', '19.3', '1234', '2']]
        'one record, null quality level'      | [['1', '2021110901', null, '19.3', '1234', '2']]
        'one record, null precipitation'      | [['1', '2021110901', '1', null, '1234', '2']]
        'one record, null RS_IND column'      | [['1', '2021110901', '1', '19.3', null, '2']]
        'one record, null precipitation type' | [['1', '2021110901', '1', '19.3', '1234', null]]
    }
}
