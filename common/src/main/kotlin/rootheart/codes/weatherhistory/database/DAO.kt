package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

interface DAO<T, E> {
    fun findAll(stationId: Long, startInclusive: LocalDate, endExclusive: LocalDate, resolution: Interval): List<T>
}

data class MinAvgMax(
    val firstDay: LocalDate,
    val min: Number?,
    val avg: Number?,
    val max: Number?,
    val details: List<Number?>
)

class MinAvgMaxDao<X : Number?>(
    private val min: Column<X>?,
    private val avg: Column<X>,
    private val max: Column<X>,
    private val details: Column<List<X>>,
) : DAO<MinAvgMax, X> {
    private val fields =
        if (min == null) MeasurementsTable.slice(MeasurementsTable.firstDay, avg, max, details)
        else MeasurementsTable.slice(MeasurementsTable.firstDay, min, avg, max, details)

    // TODO somehow specify the transaction context outside the DAO
    override fun findAll(
        stationId: Long,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        resolution: Interval
    ): List<MinAvgMax> = transaction {
        fields.select(condition(stationId, resolution, startInclusive, endExclusive))
            .map {
                MinAvgMax(
                    firstDay = it[MeasurementsTable.firstDay].toLocalDate(),
                    min = if (min == null) 0 else it[min],
                    avg = it[avg],
                    max = it[max],
                    details = it[details]
                )
            }
    }
}

data class Sum(
    val firstDay: LocalDate,
    val sum1: Number?,
    val sum2: Number?,
)

class SumDao(
    private val sum1: Column<BigDecimal?>,
    private val sum2: Column<BigDecimal?>? = null,
) : DAO<Sum, BigDecimal> {
    private val fields = if (sum2 == null) {
        MeasurementsTable.slice(MeasurementsTable.firstDay, sum1)
    } else {
        MeasurementsTable.slice(MeasurementsTable.firstDay, sum1, sum2)
    }

    // TODO somehow specify the transaction context outside the DAO
    override fun findAll(
        stationId: Long,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        resolution: Interval
    ): List<Sum> = transaction {
        fields.select(condition(stationId, resolution, startInclusive, endExclusive))
            .map {
                Sum(
                    firstDay = it[MeasurementsTable.firstDay].toLocalDate(),
                    sum1 = it[sum1] ?: 0,
                    sum2 = if (sum2 != null) it[sum2] ?: 0 else null,
                )
            }
    }
}

class Histogram(val firstDay: LocalDate, val histogram: List<Int?>)

class HistogramDao(private val column: Column<List<Int?>>) : DAO<Histogram, Int> {
    private val fields = MeasurementsTable.slice(MeasurementsTable.firstDay, column)

    // TODO somehow specify the transaction context outside the DAO
    override fun findAll(
        stationId: Long,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
        resolution: Interval
    ): List<Histogram> = transaction {
        fields.select(condition(stationId, resolution, startInclusive, endExclusive))
            .map { row -> Histogram(row[MeasurementsTable.firstDay].toLocalDate(), row[column]) }
    }
}

private fun condition(stationId: Long, interval: Interval, start: LocalDate, end: LocalDate) =
    MeasurementsTable.stationId.eq(stationId)
        .and(MeasurementsTable.interval.eq(interval))
        .and(MeasurementsTable.firstDay.greaterEq(start.toDateTimeAtStartOfDay()))
        .and(MeasurementsTable.firstDay.less(end.toDateTimeAtStartOfDay()))
