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
import rootheart.codes.weatherhistory.database.Histogram
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementColumns
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.StationDao
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
//                val month = optPathParam("month") { it.toInt() }
//                val day = optPathParam("day") { it.toInt() }
//                if (month == null) {
                val firstDay = LocalDate(year, 1, 1)
                val lastDay = firstDay.plusYears(1)
                val summary = columns.select(stationId, firstDay, lastDay, Interval.YEAR, columns::toMap)
                val details = columns.select(stationId, firstDay, lastDay, Interval.MONTH, columns::toMap)

//                } else if (day == null) {
//                    val firstDay = LocalDate(year, month, 1)
//                    val lastDay = firstDay.plusMonths(1)
//                    val summary = columns.select(stationId, firstDay, lastDay, Interval.MONTH, columns::toMap)
//                    val details = columns.select(stationId, firstDay, lastDay, Interval.DAY, columns::toMap)
//
//                } else {
//                    val firstDay = LocalDate(year, month, day)
//                    val lastDay = firstDay.plusDays(1)
//                    val details = columns.select(stationId, firstDay, lastDay, Interval.DAY, columns::toMap)
//                }

                var x = if (summary.isEmpty()) emptyMap<String, Any?>() else summary[0]
                call.respond(mapOf("summary" to x, "details" to details))
            }
        }
    }
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
    map["firstDay"] = row[MeasurementsTable.firstDay].toLocalDate()
    columns.forEach { map[it.second] = row[it.first] }
    return map
}

//private fun toMeasurement(row: ResultRow): Measurement {
//    val measurement = with(MeasurementsTable) {
//        Measurement(
//                firstDay = row[firstDay].toLocalDate(),
//                temperatures = MinAvgMax(Array(0) { BigDecimal.ZERO },
//                                         row[temperatures.min],
//                                         row[temperatures.avg],
//                                         row[temperatures.max]),
//                dewPointTemperatures = MinAvgMax(Array(0) { BigDecimal.ZERO },
//                                                 row[dewPointTemperatures.min],
//                                                 row[dewPointTemperatures.avg],
//                                                 row[dewPointTemperatures.max]),
//                humidity = MinAvgMax(Array(0) { BigDecimal.ZERO },
//                                     row[humidity.min],
//                                     row[humidity.avg],
//                                     row[humidity.max]),
//                airPressure = MinAvgMax(Array(0) { BigDecimal.ZERO },
//                                        row[humidity.min],
//                                        row[humidity.avg],
//                                        row[humidity.max]),
//                visibility = MinAvgMax(Array(0) { 0 },
//                                       row[visibility.min],
//                                       row[visibility.avg],
//                                       row[visibility.max]),
//                cloudCoverage = Histogram(row[cloudCoverage.histogram], row[cloudCoverage.details]),
//                sunshineDuration = Integers(row[sunshineDuration.details], row[sunshineDuration.sum]),
//                rainfall = Decimals(row[rainfall.details], row[rainfall.sum]),
//                snowfall = Decimals(row[snowfall.details], row[snowfall.sum]),
//                wind = MinAvgMax(Array(0) { BigDecimal.ZERO },
//                                 BigDecimal.ZERO,
//                                 row[windSpeed.avg],
//                                 row[windSpeed.max]),
//                detailedWindDirectionDegrees = row[detailedWindDirectionDegrees],
//        )
//    }
//    return measurement
//}

private fun <T> MeasurementColumns.select(stationId: Long, startInclusive: LocalDate, endExclusive: LocalDate,
                                          resolution: Interval, mapper: (ResultRow) -> T) = transaction {
    fields.select(MeasurementsTable.stationId.eq(stationId).and(MeasurementsTable.interval.eq(resolution))
                          .and(MeasurementsTable.firstDay.greaterEq(startInclusive.toDateTimeAtStartOfDay()))
                          .and(MeasurementsTable.firstDay.less(endExclusive.toDateTimeAtStartOfDay()))).map(mapper)
}

data class DayClassHistogram(val icyDays: Int, val frostyDays: Int, val vegetationDays: Int, val summerDays: Int,
                             val hotDays: Int, val desertDays: Int, val tropicNights: Int)

private data class Histograms(
        val dayClassHistogram: DayClassHistogram?,
        val sunshineHistogram: Histogram?,
        val rainfallHistogram: Histogram?,
        val maxWindSpeedHistogram: Array<Int>?,
        val humidityHistogram: Histogram?,
        val cloudCoverageHistogram: Histogram?,
)