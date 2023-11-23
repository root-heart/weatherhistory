package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.collections.AvgMaxDetails
import rootheart.codes.common.collections.Histogram
import rootheart.codes.common.collections.MinAvgMaxDetails
import rootheart.codes.common.collections.MinMaxSumDetails
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementTable
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementTable.nullable
import java.math.BigDecimal
import java.sql.Date

enum class Interval {
    DECADE, YEAR, MONTH, DAY
}

abstract class MeasurementsTable(name: String) : LongIdTable(name) {
    abstract val stationId: Column<EntityID<Long>>
    abstract val year: Column<Int>
    abstract val month: Column<Int?>
    abstract val date: Column<DateTime>

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
        val temperature: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        val dewPointTemperature: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        val humidity: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        val airPressure: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        var cloudCoverage: Histogram = Histogram(),
        val sunshine: MinMaxSumDetails<Int> = MinMaxSumDetails(),
        val rainfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val snowfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val windSpeed: AvgMaxDetails<BigDecimal> = AvgMaxDetails(),
        val visibility: MinAvgMaxDetails<Int> = MinAvgMaxDetails(),
        val detailedWindDirectionDegrees: Array<Int?>? = null
) {
    val stationId get() = station.id
    val firstDayDateTime get() = firstDay.toDateTimeAtStartOfDay()!!

}


data class MeasurementJson(
        val firstDay: LocalDate,
        val temperature: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        val dewPointTemperature: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        val humidity: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        val airPressure: MinAvgMaxDetails<BigDecimal> = MinAvgMaxDetails(),
        var cloudCoverage: List<Int> = emptyList(),
        val sunshine: MinMaxSumDetails<Int> = MinMaxSumDetails(),
        val rainfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val snowfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val windSpeed: AvgMaxDetails<BigDecimal> = AvgMaxDetails(),
        val visibility: MinAvgMaxDetails<Int> = MinAvgMaxDetails(),
)


