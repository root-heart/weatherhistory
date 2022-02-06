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

class HourlySoilTemperatureRecordConverterSpec extends Specification implements SpecUtils {
    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly soil temperature record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly soil temperature records'
        def converter = HourlySoilTemperatureRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SemicolonSeparatedValues(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'the values of each record matches the parsed values of each input line'
        records*.stationId == values.collect { it[0] != null ? StationId.of(it[0]) : null }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { it[2] != null ? QualityLevel.of(it[2]) : null }
        records*.soilTemperature2Centimeters == allBigDecimalsOf(values, 3)
        records*.soilTemperature5Centimeters == allBigDecimalsOf(values, 4)
        records*.soilTemperature10Centimeters == allBigDecimalsOf(values, 5)
        records*.soilTemperature20Centimeters == allBigDecimalsOf(values, 6)
        records*.soilTemperature50Centimeters == allBigDecimalsOf(values, 7)
        records*.soilTemperature100Centimeters == allBigDecimalsOf(values, 8)

        where:
        columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_2", "V_TE002", "V_TE005", "V_TE010", "V_TE020", "V_TE050", "V_TE100"]

        and:
        description                                       | values
        'no records'                                      | []
        'one record'                                      | [['1', '2021110901', '1', '2.2', '5.5', '10.1', '20.2', '50.5', '100.1']]
        'one record, null quality level'                  | [['1', '2021110901', null, '2.2', '5.5', '10.1', '20.2', '50.5', '100.1']]
        'one record, null temperature at 2 centimeters'   | [['1', '2021110901', '1', null, '5.5', '10.1', '20.2', '50.5', '100.1']]
        'one record, null temperature at 5 centimeters'   | [['1', '2021110901', '1', '2.2', null, '10.1', '20.2', '50.5', '100.1']]
        'one record, null temperature at 10 centimeters'  | [['1', '2021110901', '1', '2.2', '5.5', null, '20.2', '50.5', '100.1']]
        'one record, null temperature at 22 centimeters'  | [['1', '2021110901', '1', '2.2', '5.5', '10.1', null, '50.5', '100.1']]
        'one record, null temperature at 50 centimeters'  | [['1', '2021110901', '1', '2.2', '5.5', '10.1', '20.2', null, '100.1']]
        'one record, null temperature at 100 centimeters' | [['1', '2021110901', '1', '2.2', '5.5', '10.1', '20.2', '50.5', null]]
    }
}
