package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.joda.time.LocalDate
import java.math.BigDecimal

enum class Interval {
    YEAR, MONTH, DAY
}

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val firstDay = date("FIRST_DAY")
    val interval = enumerationByName("INTERVAL", 5, Interval::class)

    val temperatures = minAvgMax(decimalArray("DETAILED_AIR_TEMPERATURE_CENTIGRADE"),
                                 decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                 decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                 decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable())

    val dewPointTemperatures = minAvgMax(decimalArray("DETAILED_DEW_POINT_TEMPERATURE_CENTIGRADE"),
                                         decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                         decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                         decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable())

    val humidity = minAvgMax(decimalArray("DETAILED_HUMIDITY_PERCENT"),
                             decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable(),
                             decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable(),
                             decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable())

    val airPressure = minAvgMax(decimalArray("DETAILED_AIR_PRESSURE_HECTOPASCALS"),
                                decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
                                decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
                                decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable())

    val cloudCoverage = histogram(intArray("DETAILED_CLOUD_COVERAGE"),
                                  intArray("CLOUD_COVERAGE_HISTOGRAM"))

    val sunshineDuration = sum(intArray("DETAILED_SUNSHINE_DURATION_MINUTES"),
                               integer("SUM_SUNSHINE_DURATION_MINUTES").nullable())

    val rainfall = sum(decimalArray("DETAILED_RAINFALL_MILLIMETERS"),
                       decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable())

    val snowfall = sum(decimalArray("DETAILED_SNOWFALL_MILLIMETERS"),
                       decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable())

    val detailedWindDirectionDegrees = intArray("DETAILED_WIND_DIRECTION_DEGREES")

    val windSpeed = avgMax(details = decimalArray("DETAILED_WIND_SPEED_METERS_PER_SECOND"),
                           avg = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable(),
                           max = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable())

    val visibility = minAvgMax(intArray("DETAILED_VISIBILITY_METERS"),
                               integer("MIN_VISIBILITY_METERS").nullable(),
                               integer("AVG_VISIBILITY_METERS").nullable(),
                               integer("MAX_VISIBILITY_METERS").nullable())

    init {
        index(isUnique = true, stationId, firstDay, interval)
    }
}

private fun <T : Number?> minAvgMax(details: Column<List<T>>,
                                    min: Column<T>,
                                    avg: Column<T>,
                                    max: Column<T>) = MinAvgMaxColumns(min, avg, max, details)

private fun <T : Number?> avgMax(details: Column<List<T>>,
                                 avg: Column<T>,
                                 max: Column<T>) = AvgMaxColumns(avg, max, details)

private fun histogram(details: Column<List<Int?>>, histogram: Column<List<Int?>>) = HistogramColumns(details, histogram)

fun <X : Number?> sum(details: Column<List<X?>>, sum: Column<X?>) = SumColumns(details, sum)

object MeasurementTableMapping : TableMapping<Measurement>(
        Measurement::stationId to MeasurementsTable.stationId,
        Measurement::firstDayDateTime to MeasurementsTable.firstDay,
        Measurement::interval to MeasurementsTable.interval,

        Measurement::detailedAirTemperatureCentigrade to MeasurementsTable.temperatures.details,
        Measurement::minAirTemperatureCentigrade to MeasurementsTable.temperatures.min,
        Measurement::avgAirTemperatureCentigrade to MeasurementsTable.temperatures.avg,
        Measurement::maxAirTemperatureCentigrade to MeasurementsTable.temperatures.max,

        Measurement::detailedDewPointTemperatureCentigrade to MeasurementsTable.dewPointTemperatures.details,
        Measurement::minDewPointTemperatureCentigrade to MeasurementsTable.dewPointTemperatures.min,
        Measurement::avgDewPointTemperatureCentigrade to MeasurementsTable.dewPointTemperatures.avg,
        Measurement::maxDewPointTemperatureCentigrade to MeasurementsTable.dewPointTemperatures.max,

        Measurement::detailedHumidityPercent to MeasurementsTable.humidity.details,
        Measurement::minHumidityPercent to MeasurementsTable.humidity.min,
        Measurement::avgHumidityPercent to MeasurementsTable.humidity.avg,
        Measurement::maxHumidityPercent to MeasurementsTable.humidity.max,

        Measurement::detailedAirPressureHectopascals to MeasurementsTable.airPressure.details,
        Measurement::minAirPressureHectopascals to MeasurementsTable.airPressure.min,
        Measurement::avgAirPressureHectopascals to MeasurementsTable.airPressure.avg,
        Measurement::maxAirPressureHectopascals to MeasurementsTable.airPressure.max,

        Measurement::detailedCloudCoverages to MeasurementsTable.cloudCoverage.details,
        Measurement::cloudCoverageHistogram to MeasurementsTable.cloudCoverage.histogram,

        Measurement::detailedSunshineDurationMinutes to MeasurementsTable.sunshineDuration.details,
        Measurement::sumSunshineDurationMinutes to MeasurementsTable.sunshineDuration.sum,

        Measurement::detailedRainfallMillimeters to MeasurementsTable.rainfall.details,
        Measurement::sumRainfallMillimeters to MeasurementsTable.rainfall.sum,

        Measurement::detailedSnowfallMillimeters to MeasurementsTable.snowfall.details,
        Measurement::sumSnowfallMillimeters to MeasurementsTable.snowfall.sum,

        Measurement::detailedWindDirectionDegrees to MeasurementsTable.detailedWindDirectionDegrees,

        Measurement::detailedWindSpeedMetersPerSecond to MeasurementsTable.windSpeed.details,
        Measurement::maxWindSpeedMetersPerSecond to MeasurementsTable.windSpeed.max,
        Measurement::avgWindSpeedMetersPerSecond to MeasurementsTable.windSpeed.avg,

        Measurement::detailedVisibilityMeters to MeasurementsTable.visibility.details,
        Measurement::minVisibilityMeters to MeasurementsTable.visibility.min,
        Measurement::avgVisibilityMeters to MeasurementsTable.visibility.avg,
        Measurement::maxVisibilityMeters to MeasurementsTable.visibility.max)

class Measurement(
        @Transient var station: Station,
        @Transient var firstDay: LocalDate,
        val interval: Interval,

        val detailedAirTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
        var minAirTemperatureCentigrade: BigDecimal? = null,
        var avgAirTemperatureCentigrade: BigDecimal? = null,
        var maxAirTemperatureCentigrade: BigDecimal? = null,

        val detailedDewPointTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
        var minDewPointTemperatureCentigrade: BigDecimal? = null,
        var maxDewPointTemperatureCentigrade: BigDecimal? = null,
        var avgDewPointTemperatureCentigrade: BigDecimal? = null,

        val detailedHumidityPercent: Array<BigDecimal?> = Array(24) { null },
        var minHumidityPercent: BigDecimal? = null,
        var maxHumidityPercent: BigDecimal? = null,
        var avgHumidityPercent: BigDecimal? = null,

        val detailedAirPressureHectopascals: Array<BigDecimal?> = Array(24) { null },
        var minAirPressureHectopascals: BigDecimal? = null,
        var avgAirPressureHectopascals: BigDecimal? = null,
        var maxAirPressureHectopascals: BigDecimal? = null,

        val detailedCloudCoverages: Array<Int?> = Array(24) { null },
        var cloudCoverageHistogram: Array<Int> = Array(10) { 0 },

        val detailedSunshineDurationMinutes: Array<Int?> = Array(24) { null },
        var sumSunshineDurationMinutes: Int? = null,

        val detailedRainfallMillimeters: Array<BigDecimal?> = Array(24) { null },
        var sumRainfallMillimeters: BigDecimal? = null,

        val detailedSnowfallMillimeters: Array<BigDecimal?> = Array(24) { null },
        var sumSnowfallMillimeters: BigDecimal? = null,

        val detailedWindSpeedMetersPerSecond: Array<BigDecimal?> = Array(24) { null },
        val detailedWindDirectionDegrees: Array<Int?> = Array(24) { null },
        var maxWindSpeedMetersPerSecond: BigDecimal? = null,
        var avgWindSpeedMetersPerSecond: BigDecimal? = null,

        val detailedVisibilityMeters: Array<Int?> = Array(24) { null },
        var minVisibilityMeters: Int? = null,
        var avgVisibilityMeters: Int? = null,
        var maxVisibilityMeters: Int? = null) {
    val stationId get() = station.id
    val firstDayDateTime get() = firstDay.toDateTimeAtStartOfDay()!!
}
