package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate


class DAO<N : Number?, R>(private val columns: MeasurementColumns<N, R>) {
    fun findAll(stationId: Long, startInclusive: LocalDate, endExclusive: LocalDate, resolution: Interval) =
        transaction {
            columns.fields
                    .select(MeasurementsTable.stationId.eq(stationId)
                                    .and(MeasurementsTable.interval.eq(resolution))
                                    .and(MeasurementsTable.firstDay.greaterEq(startInclusive.toDateTimeAtStartOfDay()))
                                    .and(MeasurementsTable.firstDay.less(endExclusive.toDateTimeAtStartOfDay())))
                    .map { columns.dataObject(it) }
        }
}
