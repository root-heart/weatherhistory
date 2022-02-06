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

class HourlyMoistureRecordConverterSpec extends Specification implements SpecUtils {
    @Unroll('#description')
    def 'Test that strings are converted correctly to hourly dew point temperature record'() {
        given: 'a converter able to convert from semicolon-separated data to hourly dew point temperature records'
        def converter = HourlyMoistureRecordConverter.INSTANCE

        and: 'some data from a semicolon-separated file'
        def ssvData = new SemicolonSeparatedValues(columnNames, values.stream())

        when: 'the input is converted'
        def records = converter.convert(ssvData).collect(Collectors.toList())

        then: 'the values of each record matches the parsed values of each input line'
        records*.stationId == values.collect { it[0] != null ? StationId.of(it[0]) : null }
        records*.measurementTime == values.collect { LocalDateTime.parse(it[1], DateTimeFormatter.ofPattern('yyyyMMddHH')) }
        records*.qualityLevel == values.collect { it[2] != null ? QualityLevel.of(it[2]) : null }
        records*.absoluteHumidity == allBigDecimalsOf(values, 3)
        records*.airPressureHectopascals == allBigDecimalsOf(values, 4)
        records*.relativeHumidityPercent == allBigDecimalsOf(values, 5)
        records*.dewPointTemperatureCentigrade == allBigDecimalsOf(values, 6)
        records*.wetBulbTemperatureCentigrade == allBigDecimalsOf(values, 7)
        records*.vaporPressureHectopascals == allBigDecimalsOf(values, 8)
        records*.airTemperatureAtTwoMetersHeightCentigrade == allBigDecimalsOf(values, 9)

        where:
        columnNames = ["STATIONS_ID", "MESS_DATUM", "QN_8", "ABSF_STD", "P_STD", "RF_STD", "TD_STD", "TF_STD", "VP_STD", "TT_STD"]

        and:
        description                              | values
        'no records'                             | []
        'one record'                             | [['1', '2021110901', '1', '19.3', '1234', '77.3', '12.3', '12.1', '10.0', '100']]
        'one record, null quality level'         | [['1', '2021110901', null, '19.3', '1234', '77.3', '12.3', '12.1', '10.0', '100']]
        'one record, null absolute humidity'     | [['1', '2021110901', '1', null, '1234', '77.3', '12.3', '12.1', '10.0', '100']]
        'one record, null air pressure'          | [['1', '2021110901', '1', '19.3', null, '77.3', '12.3', '12.1', '10.0', '100']]
        'one record, null relative humidity'     | [['1', '2021110901', '1', '19.3', '1234', null, '12.3', '12.1', '10.0', '100']]
        'one record, null dew point temperature' | [['1', '2021110901', '1', '19.3', '1234', '77.3', null, '12.1', '10.0', '100']]
        'one record. null wet bulb temperature'  | [['1', '2021110901', '1', '19.3', '1234', '77.3', '12.3', null, '10.0', '100']]
        'one record, null vapor pressure'        | [['1', '2021110901', '1', '19.3', '1234', '77.3', '12.3', '12.1', null, '100']]
        'one record, null air temperature'       | [['1', '2021110901', '1', '19.3', '1234', '77.3', '12.3', '12.1', '10.0', null]]
    }
}
