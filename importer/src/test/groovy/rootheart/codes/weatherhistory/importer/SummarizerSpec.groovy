package rootheart.codes.weatherhistory.importer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Period
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.PrecipitationType
import rootheart.codes.weatherhistory.database.Station
import spock.lang.Specification
import spock.lang.Unroll

import java.math.RoundingMode

class SummarizerSpec extends Specification {
    private static random = new Random()

    def 'Test that summarizer summarizes all fields from HourlyMeasurement'() {
        given:
        def summarizer = Summarizer.INSTANCE
        def station = station()
        def date = randomDate()
        def measurement1 = hourlyMeasurement(station, date.toDateTimeAtStartOfDay().plusHours(12))
        def measurement2 = hourlyMeasurement(station, date.toDateTimeAtStartOfDay().plusHours(13))
        def measurements = [measurement1, measurement2,]

        when:
        def summarized = summarizer.summarizeHourlyRecords(station, DateInterval.&day, measurements)

        then:
        summarized.size() == 1

        summarized[0].minAirTemperatureCentigrade == minOf(measurements.collect { it.airTemperatureAtTwoMetersHeightCentigrade })
        summarized[0].avgAirTemperatureCentigrade == avgOf(measurements.collect { it.airTemperatureAtTwoMetersHeightCentigrade })
        summarized[0].maxAirTemperatureCentigrade == maxOf(measurements.collect { it.airTemperatureAtTwoMetersHeightCentigrade })

        summarized[0].minDewPointTemperatureCentigrade == minOf(measurements.collect { it.dewPointTemperatureCentigrade })
        summarized[0].avgDewPointTemperatureCentigrade == avgOf(measurements.collect { it.dewPointTemperatureCentigrade })
        summarized[0].maxDewPointTemperatureCentigrade == maxOf(measurements.collect { it.dewPointTemperatureCentigrade })

        summarized[0].minHumidityPercent == minOf(measurements.collect { it.relativeHumidityPercent })
        summarized[0].avgHumidityPercent == avgOf(measurements.collect { it.relativeHumidityPercent })
        summarized[0].maxHumidityPercent == maxOf(measurements.collect { it.relativeHumidityPercent })


        summarized[0].sumSunshineDurationHours == sumMinutesAndCalculateHours(measurements.collect { it.sunshineDurationMinutes })

        // TODO the following measurements are not yet set
//        summarized[0].sumRainfallMillimeters == measurements.collect { it.? }.sum()
//        summarized[0].sumSnowfallMillimeters == measurements.collect { it.? }.sum()

        summarized[0].avgWindSpeedMetersPerSecond == avgOf(measurements.collect { it.windSpeedMetersPerSecond })
        summarized[0].maxWindSpeedMetersPerSecond == maxOf(measurements.collect { it.maxWindSpeedMetersPerSecond })

        summarized[0].minAirPressureHectopascals == minOf(measurements.collect { it.airPressureHectopascals })
        summarized[0].avgAirPressureHectopascals == avgOf(measurements.collect { it.airPressureHectopascals })
        summarized[0].maxAirPressureHectopascals == maxOf(measurements.collect { it.airPressureHectopascals })
    }

    def 'Test that cloud coverage is calculated correctly from HourlyMeasurement'() {
        given:
        def summarizer = Summarizer.INSTANCE
        def station = station()
        def timeAtStartOfDay = randomDate().toDateTimeAtStartOfDay()
        def measurements = (0..23).collect {
            hourlyMeasurement(station, timeAtStartOfDay.plusHours(it))
        }

        when:
        def summarized = summarizer.summarizeHourlyRecords(station, DateInterval.&day, measurements)

        then:
        summarized[0].countCloudCoverage0 == measurements.findAll { it.cloudCoverage == 0 }.size()
        summarized[0].countCloudCoverage1 == measurements.findAll { it.cloudCoverage == 1 }.size()
        summarized[0].countCloudCoverage2 == measurements.findAll { it.cloudCoverage == 2 }.size()
        summarized[0].countCloudCoverage3 == measurements.findAll { it.cloudCoverage == 3 }.size()
        summarized[0].countCloudCoverage4 == measurements.findAll { it.cloudCoverage == 4 }.size()
        summarized[0].countCloudCoverage5 == measurements.findAll { it.cloudCoverage == 5 }.size()
        summarized[0].countCloudCoverage6 == measurements.findAll { it.cloudCoverage == 6 }.size()
        summarized[0].countCloudCoverage7 == measurements.findAll { it.cloudCoverage == 7 }.size()
        summarized[0].countCloudCoverage8 == measurements.findAll { it.cloudCoverage == 8 }.size()
        summarized[0].countCloudCoverageNotVisible == measurements.findAll { it.cloudCoverage == -1 }.size()
        summarized[0].countCloudCoverageNotMeasured == measurements.findAll { it.cloudCoverage == null }.size()
    }

    @Unroll('Test that summarizer does not summarize measurements within different intervals #dateDifference')
    def 'Test that summarizer does not summarize measurements within different intervals'() {
        given:
        def summarizer = Summarizer.INSTANCE
        def station = station()
        def date = randomDate()
        def measurement1 = hourlyMeasurement(station, date.toDateTimeAtStartOfDay())
        def measurement2 = hourlyMeasurement(station, date.toDateTimeAtStartOfDay() + dateDifference)
        def measurements = [measurement1, measurement2,]

        when:
        def summarized = summarizer.summarizeHourlyRecords(station, intervalCalculator, measurements)

        then:
        summarized.size() == 2

        where:
        dateDifference   | intervalCalculator
        Period.days(1)   | DateInterval.&day
        Period.months(1) | DateInterval.&month
        Period.years(1)  | DateInterval.&year
        Period.years(10) | DateInterval.&decade
    }

    static Station station() {
        new Station(1, "001", "name", "state", 10, 12.0, 2.0)
    }

    static HourlyMeasurement hourlyMeasurement(Station station, DateTime measurementTime) {
        return new HourlyMeasurement(
                random.nextLong(),
                station,
                measurementTime,
                airTemperature(),
                humidity(),
                cloudCoverage(),
                dewPointTemperature(),
                airPressure(),
                precipitation(),
                PrecipitationType.DEW,
                sunshineDuration(),
                windSpeed(),
                windSpeed(),
                windDirection(),
                visibility()
        )
    }

    static LocalDate randomDate() {
        new LocalDate(randomInt(1900, 2020), 1, 1)
                .plusMonths(randomInt(0, 12))
                .plusDays(randomInt(0, 31))
    }

    static BigDecimal airTemperature() {
        randomBigDecimal(-20, 30)
    }

    static BigDecimal humidity() {
        randomBigDecimal(0, 100)
    }

    private static Integer cloudCoverage() {
        def value = randomInt(-1, 10)
        return value > 8 ? null : value
    }

    private static BigDecimal dewPointTemperature() {
        randomBigDecimal(-20, 30)
    }

    private static BigDecimal airPressure() {
        randomBigDecimal(900, 1100)
    }

    private static BigDecimal precipitation() {
        randomBigDecimal(0, 100)
    }

    static BigDecimal sunshineDuration() {
        randomBigDecimal(0, 60)
    }

    static BigDecimal windSpeed() {
        randomBigDecimal(0, 60)
    }

    static BigDecimal windDirection() {
        randomBigDecimal(0, 360)
    }

    static BigDecimal visibility() {
        randomBigDecimal(0, 50_000)
    }

    static BigDecimal randomBigDecimal(int from, int to) {
        (random.nextInt(to * 10 - from * 10) + from * 10) / 10.0
    }

    static int randomInt(int from, int to) {
        random.nextInt(to - from) + from
    }

    static BigDecimal minOf(Collection<BigDecimal> values) {
        values.min()
    }

    static BigDecimal maxOf(Collection<BigDecimal> values) {
        values.max()
    }

    static BigDecimal sumMinutesAndCalculateHours(Collection<BigDecimal> minutes) {
        def sumMinutes = minutes.sum() as BigDecimal
        sumMinutes.divide(BigDecimal.valueOf(60), RoundingMode.HALF_UP)
    }

    static BigDecimal avgOf(Collection<BigDecimal> values) {
        def sum = values.sum() as BigDecimal
        return sum.divide(BigDecimal.valueOf(values.size()), RoundingMode.HALF_UP)
    }
}
