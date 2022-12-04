package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
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
class JdbcDAO<X : Number?>(
    private val min: Column<X>,
    private val avg: Column<X>,
    private val max: Column<X>,
    private val details: Column<List<X>>,
) {

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