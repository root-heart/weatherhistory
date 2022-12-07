package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
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
        // TODO this should be put outside the DAO
        fields.select(condition(stationId, resolution, startInclusive, endExclusive))
            .map(::mapToMinAvgMax)
    }

    private fun mapToMinAvgMax(row: ResultRow) = MinAvgMax(
        firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
        min = if (min == null) 0 else row[min],
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
    private val fields = if (sum2 == null) {
        MeasurementsTable.slice(MeasurementsTable.firstDay, sum1)
    } else {
        MeasurementsTable.slice(MeasurementsTable.firstDay, sum1, sum2)
    }

    // TODO somehow specify the transaction context outside the DAO
    override fun findAll(stationId: Long, startInclusive: LocalDate, endExclusive: LocalDate, resolution: Interval): List<Sum> = transaction {
        fields.select(condition(stationId, resolution, startInclusive, endExclusive))
            .map(::mapToMinAvgMax)
    }

    private fun mapToMinAvgMax(row: ResultRow) = Sum(
        firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
        sum1 = row[sum1] ?: 0,
        sum2 = if (sum2 != null) row[sum2] ?: 0 else null,
    )
}


private fun condition(stationId: Long, interval: Interval, start: LocalDate, end: LocalDate) =
    MeasurementsTable.stationId.eq(stationId)
        .and(MeasurementsTable.interval.eq(interval))
        .and(MeasurementsTable.firstDay.greaterEq(start.toDateTimeAtStartOfDay()))
        .and(MeasurementsTable.firstDay.less(end.toDateTimeAtStartOfDay()))
