package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.measureAndLogDuration
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

fun <T> Any.measureTransaction(identifier: String, statement: Transaction.() -> T): T =
    measureAndLogDuration(identifier) { transaction { statement() } }

interface DAO<T, E> {
    fun findAll(
        stationId: Long,
        year: Int,
        month: Int?,
        day: Int?,
        interval: Interval
    ): List<T>
}

data class MinAvgMax(
    val firstDay: LocalDate,
    val min: Number?,
    val avg: Number?,
    val max: Number?,
    val details: List<Number?>
)

class MinAvgMaxDao<X : Number?>(
    private val min: Column<X>,
    private val avg: Column<X>,
    private val max: Column<X>,
    private val details: Column<List<X>>,
) : DAO<MinAvgMax, X> {
    private val fields = MeasurementsTable.slice(MeasurementsTable.firstDay, min, avg, max, details)

    // TODO somehow specify the transaction context outside the DAO
    override fun findAll(
        stationId: Long,
        year: Int,
        month: Int?,
        day: Int?,
        interval: Interval
    ): List<MinAvgMax> = transaction {
        // TODO this should be put outside the DAO
        val start = LocalDate(year, month ?: 1, day ?: 1).toDateTimeAtStartOfDay()
        val end = calcEnd(start, month, day).minusMillis(1)
        fields.select(condition(stationId, interval, start, end))
            .map(::mapToMinAvgMax)
    }

    private fun calcEnd(start: DateTime, month: Int?, day: Int?): DateTime {
        if (month == null) {
            return start.plusYears(1)
        }
        if (day == null) {
            return start.plusMonths(1)
        }
        return start.plusDays(1).minusMillis(1)
    }

    private fun condition(stationId: Long, interval: Interval, start: DateTime, end: DateTime) =
        MeasurementsTable.stationId.eq(stationId)
            .and(MeasurementsTable.interval.eq(interval))
            .and(MeasurementsTable.firstDay.greaterEq(start))
            .and(MeasurementsTable.firstDay.less(end))

    private fun mapToMinAvgMax(row: ResultRow) = MinAvgMax(
        firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
        min = row[min],
        avg = row[avg],
        max = row[max],
        details = row[details]
    )
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
    private val fields = if ( sum2 == null) {
        MeasurementsTable.slice(MeasurementsTable.firstDay, sum1)
    } else {
        MeasurementsTable.slice(MeasurementsTable.firstDay, sum1, sum2)
    }

    // TODO somehow specify the transaction context outside the DAO
    override fun findAll(
        stationId: Long,
        year: Int,
        month: Int?,
        day: Int?,
        interval: Interval
    ): List<Sum> = transaction {
        // TODO this should be put outside the DAO
        val start = LocalDate(year, month ?: 1, day ?: 1).toDateTimeAtStartOfDay()
        val end = calcEnd(start, month, day).minusMillis(1)
        fields.select(condition(stationId, interval, start, end))
            .map(::mapToMinAvgMax)
    }

    private fun calcEnd(start: DateTime, month: Int?, day: Int?): DateTime {
        if (month == null) {
            return start.plusYears(1)
        }
        if (day == null) {
            return start.plusMonths(1)
        }
        return start.plusDays(1).minusMillis(1)
    }

    private fun condition(stationId: Long, interval: Interval, start: DateTime, end: DateTime) =
        MeasurementsTable.stationId.eq(stationId)
            .and(MeasurementsTable.interval.eq(interval))
            .and(MeasurementsTable.firstDay.greaterEq(start))
            .and(MeasurementsTable.firstDay.less(end))

    private fun mapToMinAvgMax(row: ResultRow) = Sum(
        firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
        sum1 = row[sum1] ?: 0,
        sum2 = if (sum2 != null) row[sum2] ?: 0 else null,
    )
}