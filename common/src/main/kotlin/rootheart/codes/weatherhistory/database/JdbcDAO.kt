package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.strings.splitAndTrimTokensToList
import java.math.BigDecimal
import java.sql.Date
import java.sql.ResultSet

private val log = KotlinLogging.logger {}

/**
 * Using JDBC improves performance by ~60%.
 *
 * Measuring response times (in millis) of 5590 HTTP request yielded the following results
 *             min    avg    max
 * exposed     562   1175   6738
 * jdbc        362    748   4248
 *
 * However, JDBC is much less readable than Exposed. So I decided to stick with exposed, but keep the original
 * JDBC implementation
 */
class JdbcMinAvgMaxDao<X : Number?>(
    private val min: Column<X>,
    private val avg: Column<X>,
    private val max: Column<X>,
    private val details: Column<List<X>>,
) : DAO<MinAvgMax, X> {
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
        jdbc(stationId, year, month, day, interval)
    }

    private fun jdbc(
        stationId: Long,
        year: Int,
        month: Int?,
        day: Int?,
        interval: Interval
    ): List<MinAvgMax> {
        val sql = "select ${MeasurementsTable.firstDay.name} as firstDay, " +
                "${min.name} as min, " +
                "${avg.name} as avg, " +
                "${max.name} as max, " +
                "${details.name} as details " +
                "from ${MeasurementsTable.tableName} " +
                "where ${MeasurementsTable.stationId.name} = ? " +
                "and ${MeasurementsTable.interval.name} = ? " +
                "and ${MeasurementsTable.firstDay.name} >= ? " +
                "and ${MeasurementsTable.firstDay.name} < ?"
        val start = LocalDate(year, month ?: 1, day ?: 1).toDateTimeAtStartOfDay()
        val end = calcEnd(start, month, day)
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

    private fun calcEnd(start: DateTime, month: Int?, day: Int?): DateTime {
        if (month == null) {
            return start.plusYears(1)
        }
        if (day == null) {
            return start.plusMonths(1)
        }
        return start.plusDays(1)
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