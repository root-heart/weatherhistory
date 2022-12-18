package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementColumns
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.restapp.optPathParam
import rootheart.codes.weatherhistory.restapp.optQueryParam
import rootheart.codes.weatherhistory.restapp.requiredPathParam

fun Routing.stationsResource() {
    route("stations") {
        get { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

        route("{stationId}") {
            get {
                val stationId = requiredPathParam("stationId") { it.toLong() }
                call.respond(StationDao.findById(stationId) ?: HttpStatusCode.NotFound)
            }

            get("{measurementType}/{year}/{month?}/{day?}") {
                val stationId = requiredPathParam("stationId") { it.toLong() }
                val columns = requiredPathParam("measurementType") { measurementTypeColumnsMapping[it] }
                val year = requiredPathParam("year") { it.toInt() }
                val month = optPathParam("month") { it.toInt() }
                val day = optPathParam("day") { it.toInt() }
                val firstDay = LocalDate(year, month ?: 1, day ?: 1)
                val lastDay =
                    if (month == null) firstDay.plusYears(1)
                    else if (day == null) firstDay.plusMonths(1)
                    else firstDay.plusDays(1)
                val resolution = optQueryParam("resolution") { requestResolutionToIntervalMapping[it] }
                        ?: if (month == null) Interval.MONTH else Interval.DAY
                val data = columns.select(stationId, firstDay, lastDay, resolution, columns::toMap)
                call.respond(data)
            }
        }
    }
}

val measurementTypeColumnsMapping = mapOf(
        "temperature" to MeasurementsTable.temperatures,
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

private val requestResolutionToIntervalMapping = mapOf("daily" to Interval.DAY,
                                                       "monthly" to Interval.MONTH,
                                                       "yearly" to Interval.YEAR)

private fun MeasurementColumns.toMap(row: ResultRow): Map<String, Any?> {
    val map = HashMap<String, Any?>()
    map["firstDay"] = row[MeasurementsTable.firstDay].toLocalDate()
    columns.forEach { map[it.second] = row[it.first] }
    return map
}

private fun <T> MeasurementColumns.select(stationId: Long, startInclusive: LocalDate, endExclusive: LocalDate,
                                          resolution: Interval, mapper: (ResultRow) -> T) =
    transaction {
        fields.select(MeasurementsTable.stationId.eq(stationId)
                              .and(MeasurementsTable.interval.eq(resolution))
                              .and(MeasurementsTable.firstDay.greaterEq(startInclusive.toDateTimeAtStartOfDay()))
                              .and(MeasurementsTable.firstDay.less(endExclusive.toDateTimeAtStartOfDay())))
                .map(mapper)
    }