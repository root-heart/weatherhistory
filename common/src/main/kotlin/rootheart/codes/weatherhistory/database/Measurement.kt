package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.collections.AvgMax
import rootheart.codes.common.collections.Histogram
import rootheart.codes.common.collections.MinAvgMax
import rootheart.codes.common.collections.MinMaxSumDetails
import java.math.BigDecimal

enum class Interval {
    DECADE, YEAR, MONTH, DAY
}

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val year = integer("YEAR")
    val month = integer("MONTH").nullable()
    val day = integer("DAY").nullable()
    val firstDay = date("FIRST_DAY")
    val interval = enumerationByName("INTERVAL", 5, Interval::class)

    val temperatures = MinAvgMaxDetailsColumns(
            decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            date("MIN_AIR_TEMPERATURE_DAY").nullable(),
            decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            date("MAX_AIR_TEMPERATURE_DAY").nullable(),
            decimalArrayNullable("DETAILED_AIR_TEMPERATURE_CENTIGRADE")
    )

    val dewPointTemperatures = MinAvgMaxDetailsColumns(
            decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            date("MIN_DEW_POINT_TEMPERATURE_DAY").nullable(),
            decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            date("MAX_DEW_POINT_TEMPERATURE_DAY").nullable(),
            decimalArrayNullable("DETAILED_DEW_POINT_TEMPERATURE_CENTIGRADE")
    )

    val humidity = MinAvgMaxDetailsColumns(
            decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable(),
            date("MIN_HUMIDITY_DAY").nullable(),
            decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable(),
            decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable(),
            date("MAX_HUMIDITY_DAY").nullable(),
            decimalArrayNullable("DETAILED_HUMIDITY_PERCENT")
    )

    val airPressure = MinAvgMaxDetailsColumns(
            decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
            date("MIN_AIR_PRESSURE_DAY").nullable(),
            decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
            decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
            date("MAX_AIR_PRESSURE_DAY").nullable(),
            decimalArrayNullable("DETAILED_AIR_PRESSURE_HECTOPASCALS")
    )

    val cloudCoverage = HistogramColumns(
            intArrayNullable("DETAILED_CLOUD_COVERAGE"),
            intArray("CLOUD_COVERAGE_HISTOGRAM")
    )

    val sunshine = MinMaxSumDetailsColumns(
            integer("MIN_SUNSHINE_DURATION_MINUTES").nullable(),
            date("MIN_SUNSHINE_DURATION_DAY").nullable(),
            integer("MAX_SUNSHINE_DURATION_MINUTES").nullable(),
            date("MAX_SUNSHINE_DURATION_DAY").nullable(),
            integer("SUM_SUNSHINE_DURATION_MINUTES").nullable(),
            intArrayNullable("DETAILED_SUNSHINE_DURATION_MINUTES")
    )

    val rainfall = MinMaxSumDetailsColumns(
            decimal("MIN_RAINFALL_MILLIMETERS", 6, 1).nullable(),
            date("MIN_RAINFALL_DAY").nullable(),
            decimal("MAX_RAINFALL_MILLIMETERS", 6, 1).nullable(),
            date("MAX_RAINFALL_DAY").nullable(),
            decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable(),
            decimalArrayNullable("DETAILED_RAINFALL_MILLIMETERS")
    )

    val snowfall = MinMaxSumDetailsColumns(
            decimal("MIN_SNOWFALL_MILLIMETERS", 6, 1).nullable(),
            date("MIN_SNOWFALL_DAY").nullable(),
            decimal("MAX_SNOWFALL_MILLIMETERS", 6, 1).nullable(),
            date("MAX_SNOWFALL_DAY").nullable(),
            decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable(),
            decimalArrayNullable("DETAILED_SNOWFALL_MILLIMETERS")
    )

    val detailedWindDirectionDegrees = intArrayNullable("DETAILED_WIND_DIRECTION_DEGREES")

    val windSpeed = AvgMaxDetailsColumns(
            decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable(),
            decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable(),
            date("MAX_WIND_SPEED_DAY").nullable(),
            decimalArrayNullable("DETAILED_WIND_SPEED_METERS_PER_SECOND")
    )

    val visibility = MinAvgMaxDetailsColumns(
            integer("MIN_VISIBILITY_METERS").nullable(),
            date("MIN_VISIBILITY_DAY").nullable(),
            integer("AVG_VISIBILITY_METERS").nullable(),
            integer("MAX_VISIBILITY_METERS").nullable(),
            date("MAX_VISIBILITY_DAY").nullable(),
            intArrayNullable("DETAILED_VISIBILITY_METERS")
    )

    init {
        index(isUnique = true, stationId, year, month, day, interval)
    }
}

class MinAvgMaxDetailsColumns<N : Number>(
        val min: Column<N?>,
        val minDay: Column<DateTime?>,
        val avg: Column<N?>,
        val max: Column<N?>,
        val maxDay: Column<DateTime?>,
        val details: Column<Array<N?>?>
)

// TODO this is code-wise equivalent to MinAvgMaxDetailsColumns, but the sum of e.g. the temperature seems weird
class MinMaxSumDetailsColumns<N : Number>(
        val min: Column<N?>,
        val minDay: Column<DateTime?>,
        val max: Column<N?>,
        val maxDay: Column<DateTime?>,
        val sum: Column<N?>,
        val details: Column<Array<N?>?>
)

class AvgMaxDetailsColumns<N : Number?>(
        var avg: Column<N?>,
        var max: Column<N?>,
        val maxDay: Column<DateTime?>,
        var details: Column<Array<N?>?>
)

class HistogramColumns(val details: Column<Array<Int?>?>, val histogram: Column<Array<Int>>)


//class MinAvgMax<N : Number?>(var details: Array<N?>, var min: N? = null, var avg: N? = null, var max: N? = null)
//
//
//class IntegersColumns(var details: Column<Array<Int?>>, var sum: Column<Int?>)
//
//class DecimalsColumns(var details: Column<Array<BigDecimal?>>, var sum: Column<BigDecimal?>)
//class Decimals(val values: Array<BigDecimal?>, var sum: BigDecimal? = null)
//
//class Integers(val values: Array<Int?>, var sum: Int? = null)
//
//



data class MeasurementEntity(
        val firstDay: LocalDate,
        val station: Station,
        val interval: Interval,
        val temperature: MinAvgMax<BigDecimal> = MinAvgMax(),
        val dewPointTemperature: MinAvgMax<BigDecimal> = MinAvgMax(),
        val humidity: MinAvgMax<BigDecimal> = MinAvgMax(),
        val airPressure: MinAvgMax<BigDecimal> = MinAvgMax(),
        var cloudCoverage: Histogram = Histogram(),
        val sunshine: MinMaxSumDetails<Int> = MinMaxSumDetails(),
        val rainfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val snowfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val windSpeed: AvgMax<BigDecimal> = AvgMax(),
        val visibility: MinAvgMax<Int> = MinAvgMax(),
        val detailedWindDirectionDegrees: Array<Int?>? = null
) {
    val stationId get() = station.id
    val firstDayDateTime get() = firstDay.toDateTimeAtStartOfDay()!!

}


data class MeasurementJson(
        val firstDay: LocalDate,
        val temperature: MinAvgMax<BigDecimal> = MinAvgMax(),
        val dewPointTemperature: MinAvgMax<BigDecimal> = MinAvgMax(),
        val humidity: MinAvgMax<BigDecimal> = MinAvgMax(),
        val airPressure: MinAvgMax<BigDecimal> = MinAvgMax(),
        var cloudCoverage: List<Int> = emptyList(),
        val sunshine: MinMaxSumDetails<Int> = MinMaxSumDetails(),
        val rainfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val snowfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val windSpeed: AvgMax<BigDecimal> = AvgMax(),
        val visibility: MinAvgMax<Int> = MinAvgMax(),
)

