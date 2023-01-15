package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementColumns
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.restapp.optPathParam
import rootheart.codes.weatherhistory.restapp.requiredPathParam
import java.lang.IllegalArgumentException

fun Routing.stationsResource() {
    route("stations") {
        get { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

        route("{stationId}") {
            get {
                val stationId = requiredPathParam("stationId") { it.toLong() }
                call.respond(StationDao.findById(stationId) ?: HttpStatusCode.NotFound)
            }

            get("{measurementType}/{years?}/{months?}") {
                val stationId = requiredPathParam("stationId") { it.toLong() }
                val columns = requiredPathParam("measurementType") { measurementTypeColumnsMapping[it] }
                val years = requiredPathParam("years") { toInterval(it) }
                val months = optPathParam("months") { toIntervalList(it) }

                val yearDifference = years.end - years.start
                if (months == null) {
                    val resolution = if (yearDifference > 12) {
                        Interval.YEAR
                    } else {
                        Interval.MONTH
                    }


                    val data = transaction {
                        columns.fields //select(stationId, years.start, years.end, monthsList, resolution, columns::toMap)
                                .select(MeasurementsTable.stationId.eq(stationId)
                                                .and(MeasurementsTable.interval.eq(resolution))
                                                .and(MeasurementsTable.year.greaterEq(years.start))
                                                .and(MeasurementsTable.year.lessEq(years.end)))
                                .orderBy(MeasurementsTable.year to SortOrder.ASC,
                                         MeasurementsTable.month to SortOrder.ASC,
                                         MeasurementsTable.day to SortOrder.ASC)
                                .map(columns::toMap)
                    }
                    call.respond(mapOf("summary" to data, "details" to data, "resolution" to if (resolution == Interval.YEAR) "year" else "month"))
                } else {
                    val monthsList = months.map { it.elements() }.flatten()
                    val monthsCount = monthsList.size * (yearDifference + 1)
                    if (monthsCount > 100) {
                        // TODO generate one value aggregated per year
                    } else if (monthsCount > 5) {
                        val data = transaction {
                            columns.fields //select(stationId, years.start, years.end, monthsList, resolution, columns::toMap)
                                    .select(MeasurementsTable.stationId.eq(stationId)
                                                    .and(MeasurementsTable.interval.eq(Interval.MONTH))
                                                    .and(MeasurementsTable.year.greaterEq(years.start))
                                                    .and(MeasurementsTable.year.lessEq(years.end))
                                                    .and(MeasurementsTable.month.inList(monthsList)))
                                    .orderBy(MeasurementsTable.year to SortOrder.ASC,
                                             MeasurementsTable.month to SortOrder.ASC,
                                             MeasurementsTable.day to SortOrder.ASC)
                                    .map(columns::toMap)
                        }
                        call.respond(mapOf("summary" to data, "details" to data, "resolution" to "month"))
                    } else {
                        val data = transaction {
                            columns.fields //select(stationId, years.start, years.end, monthsList, resolution, columns::toMap)
                                    .select(MeasurementsTable.stationId.eq(stationId)
                                                    .and(MeasurementsTable.interval.eq(Interval.DAY))
                                                    .and(MeasurementsTable.year.greaterEq(years.start))
                                                    .and(MeasurementsTable.year.lessEq(years.end))
                                                    .and(MeasurementsTable.month.inList(monthsList)))
                                    .orderBy(MeasurementsTable.year to SortOrder.ASC,
                                             MeasurementsTable.month to SortOrder.ASC,
                                             MeasurementsTable.day to SortOrder.ASC)
                                    .map(columns::toMap)
                        }
                        call.respond(mapOf("summary" to data, "details" to data, "resolution" to "day"))
                    }
                }
            }
        }
    }
}

private data class NumberInterval(val start: Int, val end: Int) {
    fun elements(): List<Int> = (start..end).distinct()
}

private val intervalRegex = Regex("(?<start>\\d+)(-(?<end>\\d+))?")
private fun toInterval(string: String): NumberInterval {
    val found = intervalRegex.find(string)
    if (found != null) {
        val groups = found.groups as MatchNamedGroupCollection
        val start = groups["start"]!!.value.toInt()
        val end = groups["end"]?.value?.toInt() ?: start
        return NumberInterval(start, end)
    }
    throw IllegalArgumentException()
}

private fun toIntervalList(string: String): List<NumberInterval> {
    return string.split(',').map { it.trim() }.map { toInterval(it) }
}

val measurementTypeColumnsMapping = mapOf("temperature" to MeasurementsTable.temperatures,
                                          "air-pressure" to MeasurementsTable.airPressure,
                                          "dew-point-temperature" to MeasurementsTable.dewPointTemperatures,
                                          "humidity" to MeasurementsTable.humidity,
                                          "visibility" to MeasurementsTable.visibility,
                                          "wind-speed" to MeasurementsTable.windSpeed,
                                          "sunshine-duration" to MeasurementsTable.sunshineDuration,
                                          "rainfall" to MeasurementsTable.rainfall,
                                          "snowfall" to MeasurementsTable.snowfall,
                                          "cloud-coverage" to MeasurementsTable.cloudCoverage,
                                          "summary" to MeasurementsTable.summaryColumns)

private val requestResolutionToIntervalMapping =
        mapOf("daily" to Interval.DAY, "monthly" to Interval.MONTH, "yearly" to Interval.YEAR)

private fun MeasurementColumns.toMap(row: ResultRow): Map<String, Any?> {
    val map = HashMap<String, Any?>()
    map["firstDay"] =
            LocalDate(row[MeasurementsTable.year], row[MeasurementsTable.month] ?: 1, row[MeasurementsTable.day] ?: 1)
    columns.forEach { map[it.second] = row[it.first] }
    return map
}

//private fun <T> MeasurementColumns.select(stationId: Long, startInclusive: LocalDate, endExclusive: LocalDate,
//                                          resolution: Interval, mapper: (ResultRow) -> T) = transaction {
//    fields.select(MeasurementsTable.stationId.eq(stationId).and(MeasurementsTable.interval.eq(resolution))
//                          .and(MeasurementsTable.firstDay.greaterEq(startInclusive.toDateTimeAtStartOfDay()))
//                          .and(MeasurementsTable.firstDay.less(endExclusive.toDateTimeAtStartOfDay()))).map(mapper)
//}

private fun <T> MeasurementColumns.select(stationId: Long, yearFrom: Int, yearTo: Int, months: List<Int>,
                                          resolution: Interval, mapper: (ResultRow) -> T) = transaction {
    fields.select(MeasurementsTable.stationId.eq(stationId).and(MeasurementsTable.interval.eq(resolution))
                          .and(MeasurementsTable.year.greaterEq(yearFrom))
                          .and(MeasurementsTable.year.lessEq(yearTo))
                          .and(MeasurementsTable.month.inList(months)))
            .orderBy(MeasurementsTable.year to SortOrder.ASC,
                     MeasurementsTable.month to SortOrder.ASC,
                     MeasurementsTable.day to SortOrder.ASC)
            .map(mapper)
}

data class DayClassHistogram(val icyDays: Int, val frostyDays: Int, val vegetationDays: Int, val summerDays: Int,
                             val hotDays: Int, val desertDays: Int, val tropicNights: Int)
