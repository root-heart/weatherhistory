package rootheart.codes.weatherhistory.database.daily

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years

object DailyMeasurementDao {
    fun getFirstAndLastDay(stationId: Long, firstYear: Int, lastYear: Int): FirstAndLastDay =
        with(DailyMeasurementTable) {
            val condition = this.stationId.eq(stationId)
                    .and(year.greaterEq(firstYear))
                    .and(year.lessEq(lastYear))

            slice(date.min(), date.max())
                    .select(condition)
                    .map { FirstAndLastDay(it[date.min()]!!.toLocalDate(), it[date.max()]!!.toLocalDate()) }
                    .first()
        }

    fun <T> fetchMeasurements(stationId: Long, firstYear: Int, lastYear: Int, mapper: (ResultRow) -> T) {
        with(DailyMeasurementTable) {
            val condition = this.stationId.eq(stationId)
                    .and(year.greaterEq(firstYear))
                    .and(year.lessEq(lastYear))

            select(condition).map(mapper)
        }
    }
}



data class FirstAndLastDay(val firstDay: LocalDate, val lastDay: LocalDate) {
    val monthsCount get() = Months.monthsBetween(firstDay, lastDay.plusMonths(1)).months
    val yearsCount get() = Years.yearsBetween(firstDay, lastDay.plusYears(1)).years
}