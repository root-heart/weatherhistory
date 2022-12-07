package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate

private val log = KotlinLogging.logger {}

interface Columns<N : Number?, R> {
    val fields: FieldSet

    fun dataObject(resultRow: ResultRow): R
}

class MinAvgMaxColumns<N : Number?>(
        val min: Column<N>,
        val avg: Column<N>,
        val max: Column<N>,
        val details: Column<List<N>>) : Columns<N, MinAvgMax> {
    override val fields
        get() = MeasurementsTable.slice(listOfNotNull(MeasurementsTable.firstDay, min, avg, max, details))

    override fun dataObject(resultRow: ResultRow) =
        MinAvgMax(firstDay = resultRow[MeasurementsTable.firstDay].toLocalDate(),
                  min = resultRow[min],
                  avg = resultRow[avg],
                  max = resultRow[max],
                  details = resultRow[details])
}

data class MinAvgMax(val firstDay: LocalDate,
                     val min: Number?,
                     val avg: Number?,
                     val max: Number?,
                     val details: List<Number?>)

class AvgMaxColumns<N : Number?>(
        val avg: Column<N>,
        val max: Column<N>,
        val details: Column<List<N>>) : Columns<N, MinAvgMax> {
    override val fields get() = MeasurementsTable.slice(listOfNotNull(MeasurementsTable.firstDay, avg, max, details))
    override fun dataObject(resultRow: ResultRow) =
        MinAvgMax(firstDay = resultRow[MeasurementsTable.firstDay].toLocalDate(),
                  min = null,
                  avg = resultRow[avg],
                  max = resultRow[max],
                  details = resultRow[details])
}

class SumColumns<X : Number?>(val details: Column<List<X?>>, val sum: Column<X?>) : Columns<X, Sum> {
    override val fields get() = MeasurementsTable.slice(MeasurementsTable.firstDay, sum, details)
    override fun dataObject(resultRow: ResultRow) = Sum(firstDay = resultRow[MeasurementsTable.firstDay].toLocalDate(),
                                                        sum = resultRow[sum])
}

data class Sum(val firstDay: LocalDate, val sum: Number?)

class HistogramColumns(val details: Column<List<Int?>>, val histogram: Column<List<Int?>>) : Columns<Int?, Histogram> {
    override val fields get() = MeasurementsTable.slice(MeasurementsTable.firstDay, histogram, details)
    override fun dataObject(resultRow: ResultRow) =
        Histogram(resultRow[MeasurementsTable.firstDay].toLocalDate(), resultRow[histogram])

}

class Histogram(val firstDay: LocalDate, val histogram: List<Int?>)

class DAO<N : Number?, R>(private val columns: Columns<N, R>) {
    fun findAll(stationId: Long, startInclusive: LocalDate, endExclusive: LocalDate, resolution: Interval) =
        transaction {
            columns.fields
                    .select(condition(stationId, resolution, startInclusive, endExclusive))
                    .map { columns.dataObject(it) }
        }
}

private fun condition(stationId: Long, interval: Interval, start: LocalDate, end: LocalDate) =
    MeasurementsTable.stationId.eq(stationId)
            .and(MeasurementsTable.interval.eq(interval))
            .and(MeasurementsTable.firstDay.greaterEq(start.toDateTimeAtStartOfDay()))
            .and(MeasurementsTable.firstDay.less(end.toDateTimeAtStartOfDay()))
