package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.LocalDate
import java.math.BigDecimal

enum class Interval {
    YEAR, MONTH, DAY
}

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val firstDay = date("FIRST_DAY")
    val interval = enumerationByName("INTERVAL", 5, Interval::class)

    val temperatures = MinAvgMaxColumns(
            decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimalArray("DETAILED_AIR_TEMPERATURE_CENTIGRADE")
    )

    val dewPointTemperatures = MinAvgMaxColumns(
            decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
            decimalArray("DETAILED_DEW_POINT_TEMPERATURE_CENTIGRADE")
    )

    val humidity = MinAvgMaxColumns(
            decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable(),
            decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable(),
            decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable(),
            decimalArray("DETAILED_HUMIDITY_PERCENT")
    )

    val airPressure = MinAvgMaxColumns(
            decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
            decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
            decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
            decimalArray("DETAILED_AIR_PRESSURE_HECTOPASCALS")
    )

    val cloudCoverage = HistogramColumns(
            intArrayNullable("DETAILED_CLOUD_COVERAGE"),
            intArray("CLOUD_COVERAGE_HISTOGRAM")
    )

    val sunshineDuration = IntegersColumns(
            intArrayNullable("DETAILED_SUNSHINE_DURATION_MINUTES"),
            integer("SUM_SUNSHINE_DURATION_MINUTES").nullable()
    )

    val rainfall = DecimalsColumns(
            decimalArray("DETAILED_RAINFALL_MILLIMETERS"),
            decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable()
    )

    val snowfall = DecimalsColumns(
            decimalArray("DETAILED_SNOWFALL_MILLIMETERS"),
            decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable()
    )

    val detailedWindDirectionDegrees = intArrayNullable("DETAILED_WIND_DIRECTION_DEGREES")

    val windSpeed = AvgMaxColumns(
            decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable(),
            decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable(),
            decimalArray("DETAILED_WIND_SPEED_METERS_PER_SECOND")
    )

    val visibility = MinAvgMaxColumns(
            integer("MIN_VISIBILITY_METERS").nullable(),
            integer("AVG_VISIBILITY_METERS").nullable(),
            integer("MAX_VISIBILITY_METERS").nullable(),
            intArrayNullable("DETAILED_VISIBILITY_METERS")
    )

    init {
        index(isUnique = true, stationId, firstDay, interval)
    }
}

// TODO Place this class and its children somewhere else as this is more or less to "JSONify" the data.
abstract class MeasurementColumns<N : Number?, R>(private val columns: Map<Column<*>, String>) {
    val fields get() = MeasurementsTable.slice(columns.keys.toList() + MeasurementsTable.firstDay)

    fun dataObject(row: ResultRow): Map<String, Any?> {
        val map = HashMap<String, Any?> ()
        map["firstDay"] = row[MeasurementsTable.firstDay].toLocalDate()
        columns.forEach {
            map[it.value] = row[it.key]
        }
        return map
    }
}

class MinAvgMaxColumns<N : Number>(val min: Column<N?>,
                                   val avg: Column<N?>,
                                   val max: Column<N?>,
                                   val details: Column<Array<N?>>) :
        MeasurementColumns<N, MinAvgMax<N>>(mapOf(min to "min", avg to "avg", max to "max", details to "details"))

class MinAvgMax<N : Number?>(var details: Array<N?>, var min: N? = null, var avg: N? = null, var max: N? = null)

class AvgMaxColumns<N : Number?>(var avg: Column<N?>, var max: Column<N?>, var details: Column<Array<N?>>) :
        MeasurementColumns<N, MinAvgMax<N?>>(mapOf(avg to "avg", max to "max", details to "details"))

class IntegersColumns(var details: Column<Array<Int?>>, var sum: Column<Int?>) :
        MeasurementColumns<Int, Integers>(mapOf(details to "details", sum to "sum"))

class DecimalsColumns(var details: Column<Array<BigDecimal?>>, var sum: Column<BigDecimal?>) :
        MeasurementColumns<Int, Decimals>(mapOf(details to "details", sum to "sum"))

class Decimals(val values: Array<BigDecimal?>, var sum: BigDecimal? = null)

class Integers(val values: Array<Int?>, var sum: Int? = null)

class HistogramColumns(val details: Column<Array<Int?>>, val histogram: Column<Array<Int>>) :
        MeasurementColumns<Int?, Histogram>(mapOf(details to "details", histogram to "histogram"))

class Histogram(var histogram: Array<Int>, var details: Array<Int?>)

class Measurement(@Transient var station: Station,
                  @Transient var firstDay: LocalDate,
                  val interval: Interval,

                  val temperatures: MinAvgMax<BigDecimal?>,
                  val dewPointTemperatures: MinAvgMax<BigDecimal?>,
                  val humidity: MinAvgMax<BigDecimal?>,
                  val airPressure: MinAvgMax<BigDecimal?>,
                  val visibility: MinAvgMax<Int?>,
                  val cloudCoverage: Histogram,
                  val sunshineDuration: Integers,
                  val rainfall: Decimals,
                  val snowfall: Decimals,
                  val wind: MinAvgMax<BigDecimal?>,
                  val detailedWindDirectionDegrees: Array<Int?>) {
    val stationId get() = station.id
    val firstDayDateTime get() = firstDay.toDateTimeAtStartOfDay()!!
}
