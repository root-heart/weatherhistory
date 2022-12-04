package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.common.strings.splitAndTrimTokensToList
import java.math.BigDecimal
import java.sql.Date
import java.sql.ResultSet

private val log = KotlinLogging.logger {}

// using JDBC improves performance by almost 100%
private const val USE_JDBC = true

fun <T> Any.measureTransaction(identifier: String, statement: Transaction.() -> T): T =
    measureAndLogDuration(identifier) { transaction { statement() } }

data class MinAvgMax(
    val firstDay: LocalDate,
    val min: Number?,
    val avg: Number?,
    val max: Number?,
    val details: List<Number?>
)

data class MeasurementColumns<X : Number?>(
    val min: Column<X>,
    val avg: Column<X>,
    val max: Column<X>,
    val details: Column<List<X>>,
) {
    override fun toString(): String {
        return "${min.name}/${avg.name}/${max.name}/${details.name}"
    }
}

object MinAvgMaxDao {
    fun <T : Number?> findAll(
        stationId: Long,
        year: Int,
        month: Int?,
        day: Int?,
        columns: MeasurementColumns<T>,
        interval: Interval
    ): List<MinAvgMax> =
        if (USE_JDBC) jdbc(stationId, year, month, day, columns, interval)
        else exposed(stationId, year, month, day, columns, interval)

    private fun <T : Number?> exposed(
        stationId: Long,
        year: Int,
        month: Int?,
        day: Int?,
        columns: MeasurementColumns<T>,
        interval: Interval
    ) = transaction {
        val start = LocalDate(year, month ?: 1, day ?: 1).toDateTimeAtStartOfDay()
        val end = when (month) {
            null -> start.plusYears(1)
            else -> when (day) {
                null -> start.plusMonths(1)
                else -> start.plusDays(1)
            }
        }.minusMillis(1)
        MeasurementsTable.slice(MeasurementsTable.firstDay, columns.min, columns.avg, columns.max, columns.details)
            .select(condition(stationId, interval, start, end))
            .map { row ->
                MinAvgMax(
                    firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
                    min = row[columns.min],
                    avg = row[columns.avg],
                    max = row[columns.max],
                    details = row[columns.details]
                )
            }
    }

    private fun condition(stationId: Long, interval: Interval, start: DateTime, end: DateTime) =
        MeasurementsTable.stationId.eq(stationId)
            .and(MeasurementsTable.interval.eq(interval))
            .and(MeasurementsTable.firstDay.between(start, end))

    private fun <T : Number?> jdbc(
        stationId: Long,
        year: Int,
        month: Int?,
        day: Int?,
        columns: MeasurementColumns<T>,
        interval: Interval
    ): List<MinAvgMax> {
        val sql = "select ${MeasurementsTable.firstDay.name} as firstDay, " +
                "${columns.min.name} as min, " +
                "${columns.avg.name} as avg, " +
                "${columns.max.name} as max, " +
                "${columns.details.name} as details " +
                "from ${MeasurementsTable.tableName} " +
                "where ${MeasurementsTable.stationId.name} = ? " +
                "and ${MeasurementsTable.interval.name} = ? " +
                "and ${MeasurementsTable.firstDay.name} between ? and ?"
        val start = LocalDate(year, month ?: 1, day ?: 1).toDateTimeAtStartOfDay()
        val end = when (month) {
            null -> start.plusYears(1)
            else -> when (day) {
                null -> start.plusMonths(1)
                else -> start.plusDays(1)
            }
        }.minusMillis(1)
        return WeatherDb.dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                log.info { "Executing $sql" }
                stmt.setLong(1, stationId)
                stmt.setString(2, interval.name)
                stmt.setDate(3, Date(start.toDate().time))
                stmt.setDate(4, Date(end.toDate().time))
                stmt.executeQuery().use { makeListFromResultSet(it) }
            }
        }
    }

    private fun makeListFromResultSet(rs: ResultSet): List<MinAvgMax> {
        val list = ArrayList<MinAvgMax>()
        while (rs.next()) {
            val detailsString = rs.getString("details")
            val detailsList = splitAndTrimTokensToList(detailsString) { BigDecimal(it) }

            list += MinAvgMax(
                firstDay = LocalDate(rs.getDate("firstDay")),
                min = rs.getBigDecimal("min"),
                avg = rs.getBigDecimal("avg"),
                max = rs.getBigDecimal("max"),
                details = detailsList
            )
        }
        return list
    }
}
