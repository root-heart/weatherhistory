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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.min
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years
import rootheart.codes.common.collections.nullsafeAvgDecimal
import rootheart.codes.common.collections.nullsafeAvgInt
import rootheart.codes.common.collections.nullsafeMax
import rootheart.codes.common.collections.nullsafeMin
import rootheart.codes.common.collections.nullsafeSumDecimals
import rootheart.codes.common.collections.nullsafeSumInts
import rootheart.codes.weatherhistory.database.Interval
import rootheart.codes.weatherhistory.database.MeasurementColumns
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.restapp.optPathParam
import rootheart.codes.weatherhistory.restapp.requiredPathParam
import java.math.BigDecimal

fun Routing.stationsResource() {
    route("stations") {
        get { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }

        route("{stationId}") {
            get {
                val stationId = requiredPathParam("stationId") { it.toLong() }
                call.respond(StationDao.findById(stationId) ?: HttpStatusCode.NotFound)
            }

            get("summary/{years?}/{months?}") {
                val stationId = requiredPathParam("stationId") { it.toLong() }

                val years = requiredPathParam("years") { toInterval(it) }
                val months = optPathParam("months") { toIntervalList(it) }

                val data = transaction {
                    var condition = MeasurementsTable.stationId.eq(stationId)
                            .and(MeasurementsTable.year.greaterEq(years.start))
                            .and(MeasurementsTable.year.lessEq(years.end))
                    if (months != null) {
                        val monthsList = months.map { it.elements() }.flatten()
                        condition = condition.and(MeasurementsTable.month.inList(monthsList))
                    }

                    val period = MeasurementsTable.slice(MeasurementsTable.firstDay.min(),
                                                         MeasurementsTable.firstDay.max())
                            .select(condition.and(MeasurementsTable.interval.eq(Interval.DAY)))
                            .map {
                                val min = it[MeasurementsTable.firstDay.min()]
                                val max = it[MeasurementsTable.firstDay.max()]
                                if (min != null && max != null) {
                                    return@map FirstAndLastDay(min.toLocalDate(), max.toLocalDate())
                                } else {
                                    return@map null
                                }
                            }
                            .first() ?: return@transaction ArrayList<MeasurementJson>()

                    val customAggregation = period.yearsCount > 2 && months != null
                    val resolution = if (period.monthsCount <= 2) {
                        Interval.DAY
                    } else if (period.yearsCount <= 2 || months != null) {
                        Interval.MONTH
                    } else {
                        Interval.YEAR
                    }

                    var data = MeasurementsTable.slice(dataColumns)
                            .select(condition.and(MeasurementsTable.interval.eq(resolution)))
                            .map(::toJson)
                            .sortedBy { it.firstDay }

                    if (customAggregation) {
                        data = data.groupBy { it.firstDay.year }
                                .mapValues { (year, list) ->
                                    val cloudCoverages = list.map { it.cloudCoverage }
                                    val sumCloudCoverages = listOf(
                                            cloudCoverages.sumOf { it[0] },
                                            cloudCoverages.sumOf { it[1] },
                                            cloudCoverages.sumOf { it[2] },
                                            cloudCoverages.sumOf { it[3] },
                                            cloudCoverages.sumOf { it[4] },
                                            cloudCoverages.sumOf { it[5] },
                                            cloudCoverages.sumOf { it[6] },
                                            cloudCoverages.sumOf { it[7] },
                                            cloudCoverages.sumOf { it[8] },
                                            cloudCoverages.sumOf { it[9] },
                                    )
                                    MeasurementJson(firstDay = LocalDate(year, 1, 1),
                                                    minTemperature = list.nullsafeMin { it.minTemperature },
                                                    avgTemperature = list.nullsafeAvgDecimal { it.avgTemperature },
                                                    maxTemperature = list.nullsafeMax { it.maxTemperature },
                                                    minDewPointTemperature = list.nullsafeMin { it.minTemperature },
                                                    avgDewPointTemperature = list.nullsafeAvgDecimal { it.avgTemperature },
                                                    maxDewPointTemperature = list.nullsafeMax { it.maxTemperature },
                                                    minHumidity = list.nullsafeMin { it.minTemperature },
                                                    avgHumidity = list.nullsafeAvgDecimal { it.avgTemperature },
                                                    maxHumidity = list.nullsafeMax { it.maxTemperature },
                                                    minAirPressure = list.nullsafeMin { it.minTemperature },
                                                    avgAirPressure = list.nullsafeAvgDecimal { it.avgTemperature },
                                                    maxAirPressure = list.nullsafeMax { it.maxTemperature },
                                                    cloudCoverage = sumCloudCoverages,
                                                    sunshineDuration = list.nullsafeSumInts { it.sunshineDuration },
                                                    rainfall = list.nullsafeSumDecimals { it.rainfall },
                                                    snowfall = list.nullsafeSumDecimals { it.snowfall },
                                                    avgWindspeed = list.nullsafeAvgDecimal { it.avgWindspeed },
                                                    maxWindspeed = list.nullsafeMax { it.maxWindspeed },
                                                    minVisibility = list.nullsafeMin { it.minVisibility },
                                                    avgVisibility = list.nullsafeAvgInt { it.avgVisibility },
                                                    maxVisibility = list.nullsafeMax { it.maxVisibility })
                                }
                                .values
                                .sortedBy { it.firstDay }
                    }

                    return@transaction data
                }

                call.respond(mapOf("summary" to data,
                                   "details" to data,
                                   "resolution" to "day"))
            }
        }
    }
}

private data class FirstAndLastDay(val firstDay: LocalDate, val lastDay: LocalDate) {
    val monthsCount get() = Months.monthsBetween(firstDay, lastDay.plusMonths(1)).months
    val yearsCount get() = Years.yearsBetween(firstDay, lastDay.plusYears(1)).years
}

private val dataColumns = listOf(MeasurementsTable.firstDay,
                                 MeasurementsTable.temperatures.min,
                                 MeasurementsTable.temperatures.avg,
                                 MeasurementsTable.temperatures.max,
                                 MeasurementsTable.dewPointTemperatures.min,
                                 MeasurementsTable.dewPointTemperatures.avg,
                                 MeasurementsTable.dewPointTemperatures.max,
                                 MeasurementsTable.humidity.min,
                                 MeasurementsTable.humidity.avg,
                                 MeasurementsTable.humidity.max,
                                 MeasurementsTable.airPressure.min,
                                 MeasurementsTable.airPressure.avg,
                                 MeasurementsTable.airPressure.max,
                                 MeasurementsTable.cloudCoverage.histogram,
                                 MeasurementsTable.sunshineDuration.sum,
                                 MeasurementsTable.rainfall.sum,
                                 MeasurementsTable.snowfall.sum,
                                 MeasurementsTable.windSpeed.avg,
                                 MeasurementsTable.windSpeed.max,
                                 MeasurementsTable.visibility.min,
                                 MeasurementsTable.visibility.avg,
                                 MeasurementsTable.visibility.max)

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

private fun MeasurementColumns.toMap(row: ResultRow): Map<String, Any?> {
    val map = HashMap<String, Any?>()
    map["firstDay"] = LocalDate(row[MeasurementsTable.year],
                                row[MeasurementsTable.month] ?: 1,
                                row[MeasurementsTable.day] ?: 1)
    columns.forEach { map[it.second] = row[it.first] }
    return map
}

private fun toJson(row: ResultRow): MeasurementJson {
    return MeasurementJson(firstDay = row[MeasurementsTable.firstDay].toLocalDate(),
                           minTemperature = row[MeasurementsTable.temperatures.min],
                           avgTemperature = row[MeasurementsTable.temperatures.avg],
                           maxTemperature = row[MeasurementsTable.temperatures.max],
                           minDewPointTemperature = row[MeasurementsTable.dewPointTemperatures.min],
                           avgDewPointTemperature = row[MeasurementsTable.dewPointTemperatures.avg],
                           maxDewPointTemperature = row[MeasurementsTable.dewPointTemperatures.max],
                           minHumidity = row[MeasurementsTable.humidity.min],
                           avgHumidity = row[MeasurementsTable.humidity.avg],
                           maxHumidity = row[MeasurementsTable.humidity.max],
                           minAirPressure = row[MeasurementsTable.airPressure.min],
                           avgAirPressure = row[MeasurementsTable.airPressure.avg],
                           maxAirPressure = row[MeasurementsTable.airPressure.max],
                           cloudCoverage = row[MeasurementsTable.cloudCoverage.histogram].toList(),
                           sunshineDuration = row[MeasurementsTable.sunshineDuration.sum],
                           rainfall = row[MeasurementsTable.rainfall.sum],
                           snowfall = row[MeasurementsTable.snowfall.sum],
                           avgWindspeed = row[MeasurementsTable.windSpeed.avg],
                           maxWindspeed = row[MeasurementsTable.windSpeed.max],
                           minVisibility = row[MeasurementsTable.visibility.min],
                           avgVisibility = row[MeasurementsTable.visibility.avg],
                           maxVisibility = row[MeasurementsTable.visibility.max])
}

data class MeasurementJson(
        val firstDay: LocalDate,
        val minTemperature: BigDecimal?,
        val avgTemperature: BigDecimal?,
        val maxTemperature: BigDecimal?,
        val minDewPointTemperature: BigDecimal?,
        val avgDewPointTemperature: BigDecimal?,
        val maxDewPointTemperature: BigDecimal?,
        val minHumidity: BigDecimal?,
        val avgHumidity: BigDecimal?,
        val maxHumidity: BigDecimal?,
        val minAirPressure: BigDecimal?,
        val avgAirPressure: BigDecimal?,
        val maxAirPressure: BigDecimal?,
        val cloudCoverage: List<Int>,
        val sunshineDuration: Int?,
        val rainfall: BigDecimal?,
        val snowfall: BigDecimal?,
        val avgWindspeed: BigDecimal?,
        val maxWindspeed: BigDecimal?,
        val minVisibility: Int?,
        val avgVisibility: Int?,
        val maxVisibility: Int?,
)