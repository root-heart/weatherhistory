package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.SpecUtils
import rootheart.codes.weatherhistory.importer.ssv.SsvData
import rootheart.codes.weatherhistory.model.StationId
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class SsvToHourlyVisibilityRecordConverterSpec extends Specification implements SpecUtils {
    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly dew point temperature record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly dew point temperature records'
        def converter = SsvToHourlyVisibilityRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SsvData(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'the values of each record matches the parsed values of each input line'
        records*.stationId == values.collect { it[0] != null ? StationId.of(it[0]) : null }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == allQualityLevelsOf(values, 2)
        records*.measurementOrObservation == allMeasurementOrObservationsOf(values, 3)
        records*.visibilityInMeters == allBigDecimalsOf(values, 4)

        where:
        columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_8", "V_VV_I", "V_VV"]

        and:
        description                                   | values
        'no records'                                  | []
        'one record'                                  | [['1', '2021110901', '1', 'P', '19.3']]
        'one record, null quality level'              | [['1', '2021110901', null, 'P', '19.3']]
        'one record, null measurement or observation' | [['1', '2021110901', '1', null, '19.3']]
        'one record, null visibility'                 | [['1', '2021110901', '1', 'P', null]]
    }
}
