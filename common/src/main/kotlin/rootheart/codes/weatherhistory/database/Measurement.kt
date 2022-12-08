package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.LocalDate
import java.math.BigDecimal
import java.util.Arrays

enum class Interval {
    YEAR, MONTH, DAY
}

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val firstDay = date("FIRST_DAY")
    val interval = enumerationByName("INTERVAL", 5, Interval::class)

    val temperatures = MinAvgMaxColumns(decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                        decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                        decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                        decimalArray("DETAILED_AIR_TEMPERATURE_CENTIGRADE"))

    val dewPointTemperatures = MinAvgMaxColumns(decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                                decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                                decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable(),
                                                decimalArray("DETAILED_DEW_POINT_TEMPERATURE_CENTIGRADE"))

    val humidity = MinAvgMaxColumns(decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable(),
                                    decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable(),
                                    decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable(),
                                    decimalArray("DETAILED_HUMIDITY_PERCENT"))

    val airPressure = MinAvgMaxColumns(decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
                                       decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
                                       decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable(),
                                       decimalArray("DETAILED_AIR_PRESSURE_HECTOPASCALS"))

    val cloudCoverage = HistogramColumns(intArray("DETAILED_CLOUD_COVERAGE"),
                                         intArray("CLOUD_COVERAGE_HISTOGRAM"))

    val sunshineDuration = SumColumns(intArray("DETAILED_SUNSHINE_DURATION_MINUTES"),
                                      integer("SUM_SUNSHINE_DURATION_MINUTES").nullable())

    val rainfall = SumColumns(decimalArray("DETAILED_RAINFALL_MILLIMETERS"),
                              decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable())

    val snowfall = SumColumns(decimalArray("DETAILED_SNOWFALL_MILLIMETERS"),
                              decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable())

    val detailedWindDirectionDegrees = intArray("DETAILED_WIND_DIRECTION_DEGREES")

    val windSpeed = AvgMaxColumns(decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable(),
                                  decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable(),
                                  decimalArray("DETAILED_WIND_SPEED_METERS_PER_SECOND"))

    val visibility = MinAvgMaxColumns(integer("MIN_VISIBILITY_METERS").nullable(),
                                      integer("AVG_VISIBILITY_METERS").nullable(),
                                      integer("MAX_VISIBILITY_METERS").nullable(),
                                      intArray("DETAILED_VISIBILITY_METERS"))

    init {
        index(isUnique = true, stationId, firstDay, interval)
    }
}

abstract class MeasurementColumns<N : Number?, R>(private vararg val columns: Column<*>) {
    val fields get() = MeasurementsTable.slice(columns.distinct() + MeasurementsTable.firstDay)

    abstract fun dataObject(row: ResultRow): R
}

class MinAvgMaxColumns<N : Number?>(val min: Column<N>, val avg: Column<N>, val max: Column<N>,
                                    val details: Column<Array<N>>) :
        MeasurementColumns<N, MinAvgMax<N>>(min, avg, max, details) {
    override fun dataObject(row: ResultRow) =
        MinAvgMax(firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
                  min = row[min],
                  avg = row[avg],
                  max = row[max],
                  details = row[details])
}

class MinAvgMax<N : Number?>(val firstDay: LocalDate, var min: N, var avg: N, var max: N,
                             var details: Array<N>)

class AvgMaxColumns<N : Number?>(var avg: Column<N?>, var max: Column<N?>, var details: Column<Array<N?>>) :
        MeasurementColumns<N, MinAvgMax<N?>>(avg, max, details) {
    override fun dataObject(row: ResultRow) =
        MinAvgMax(firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
                  min = null,
                  avg = row[avg],
                  max = row[max],
                  details = row[details])
}

class SumColumns<N : Number?>(var details: Column<Array<N?>>, var sum: Column<N?>) :
        MeasurementColumns<N, Sum<N?>>(details, sum) {
    override fun dataObject(row: ResultRow) = Sum(firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
                                                  sum = row[sum],
                                                  details = row[details])
}

class Sum<N : Number?>(val firstDay: LocalDate, var sum: N?, val details: Array<N?>)

class HistogramColumns(val details: Column<Array<Int?>>, val histogram: Column<Array<Int?>>) :
        MeasurementColumns<Int?, Histogram>(details, histogram) {
    override fun dataObject(row: ResultRow) =
        Histogram(firstDay = row[MeasurementsTable.firstDay].toLocalDate(), histogram = row[histogram],
                  details = row[details])
}

class Histogram(val firstDay: LocalDate, var histogram: Array<Int?>, val details: Array<Int?>)

class Measurement(
        @Transient var station: Station,
        @Transient var firstDay: LocalDate,
        val interval: Interval,

        val temperatures: MinAvgMax<BigDecimal?>,
        val dewPointTemperatures: MinAvgMax<BigDecimal?>,
        val humidity: MinAvgMax<BigDecimal?>,
        val airPressure: MinAvgMax<BigDecimal?>,
        val visibility: MinAvgMax<Int?>,
        val cloudCoverage: Histogram,
        val sunshineDuration: Sum<Int>,
        val rainfall: Sum<BigDecimal>,
        val snowfall: Sum<BigDecimal>,
        val wind: MinAvgMax<BigDecimal?>,
        val detailedWindDirectionDegrees: Array<Int?> = Array(0) { null }) {
    val stationId get() = station.id
    val firstDayDateTime get() = firstDay.toDateTimeAtStartOfDay()!!
}
