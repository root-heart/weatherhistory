package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import rootheart.codes.common.collections.nullsafeAvgDecimal
import rootheart.codes.common.collections.nullsafeAvgInt
import rootheart.codes.common.collections.nullsafeMax
import rootheart.codes.common.collections.nullsafeMin
import rootheart.codes.common.collections.nullsafeSumDecimals
import rootheart.codes.common.collections.nullsafeSumInts
import rootheart.codes.weatherhistory.database.*
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

                var condition = MeasurementsTable.stationId.eq(stationId)
                        .and(MeasurementsTable.year.greaterEq(years.start))
                        .and(MeasurementsTable.year.lessEq(years.end))
                if (months != null) {
                    val monthsList = months.map { it.elements() }.flatten()
                    condition = condition.and(MeasurementsTable.month.inList(monthsList))
                }

                val response = transaction {
                    val resolution = getBestResolution(condition)
                    val query = dataColumns.fields
                            .select(condition.and(MeasurementsTable.interval.eq(resolution)))
                            .orderBy(MeasurementsTable.year to SortOrder.ASC,
                                     MeasurementsTable.month to SortOrder.ASC,
                                     MeasurementsTable.day to SortOrder.ASC)
                    if (years.difference >= 1 && months != null && resolution == Interval.YEAR) {
                        // TODO check if the result would really contain more than one year
                        // if the result would contain only one year of data, the resolution might be determined as in the
                        // else branch and the custom aggregation of data would be unnecessary
                        val data = query.map(::toSummarizedData)
                        val grouped = data.groupBy { it.firstDay }
                                .mapValues { (firstDay, list) ->
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
                                    SummarizedYearData(firstDay = firstDay,
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

                        val list = grouped.values.toList().sortedBy { it.firstDay }
                        return@transaction mapOf("summary" to list,
                                                 "details" to list,
                                                 "resolution" to "year")
                    } else {
                        val data = query.map(dataColumns::toMap)
                        val resolutionString = when (resolution) {
                            Interval.YEAR  -> "year"
                            Interval.MONTH -> "month"
                            else           -> "day"
                        }
                        return@transaction mapOf("summary" to data,
                                                 "details" to data,
                                                 "resolution" to resolutionString)
                    }
                }
                call.respond(response)
            }
        }
    }
}

data class CountPerInterval(val interval: Interval, val yearCount: Int, val minMonth: Int?, val maxMonth: Int?)

private fun getBestResolution(condition: Op<Boolean>): Interval {
    val countsPerResolution = MeasurementsTable.slice(MeasurementsTable.interval,
                                                      MeasurementsTable.year.countDistinct(),
                                                      MeasurementsTable.month.min(),
                                                      MeasurementsTable.month.max())
            .select(condition)
            .groupBy(MeasurementsTable.interval)
            .map {
                CountPerInterval(interval = it[MeasurementsTable.interval],
                                 yearCount = it[MeasurementsTable.year.countDistinct()],
                                 minMonth = it[MeasurementsTable.month.min()],
                                 maxMonth = it[MeasurementsTable.month.max()])
            }
            .associateBy { it.interval }

    // wenn nur ein Jahr und maximal zwei aufeinanderfolgende Monate -> Tag
    // wenn nur ein Jahr und mehr als zwei Monate oder zwei nicht aufeinanderfolgende Monate -> Monat
    // sonst -> Jahr
    val countsForDailyResolution = countsPerResolution[Interval.DAY]
    if (countsForDailyResolution?.yearCount == 1 && countsForDailyResolution.maxMonth != null && countsForDailyResolution.minMonth != null && countsForDailyResolution.maxMonth - countsForDailyResolution.minMonth <= 2) {
        return Interval.DAY
    }
    val countsForMonthlyResolution = countsPerResolution[Interval.MONTH]
    if (countsForMonthlyResolution?.yearCount == 1) {
        return Interval.MONTH
    }
    return Interval.YEAR
}

private val dataColumns = MeasurementColumns(MeasurementsTable.temperatures.min to "minTemperature",
                                             MeasurementsTable.temperatures.avg to "avgTemperature",
                                             MeasurementsTable.temperatures.max to "maxTemperature",
                                             MeasurementsTable.dewPointTemperatures.min to "minDewPointTemperature",
                                             MeasurementsTable.dewPointTemperatures.avg to "avgDewPointTemperature",
                                             MeasurementsTable.dewPointTemperatures.max to "maxDewPointTemperature",
                                             MeasurementsTable.humidity.min to "minHumidity",
                                             MeasurementsTable.humidity.avg to "avgHumidity",
                                             MeasurementsTable.humidity.max to "maxHumidity",
                                             MeasurementsTable.airPressure.min to "minAirPressure",
                                             MeasurementsTable.airPressure.avg to "avgAirPressure",
                                             MeasurementsTable.airPressure.max to "maxAirPressure",
                                             MeasurementsTable.cloudCoverage.histogram to "cloudCoverage",
                                             MeasurementsTable.sunshineDuration.sum to "sunshineDuration",
                                             MeasurementsTable.rainfall.sum to "rainfall",
                                             MeasurementsTable.snowfall.sum to "snowfall",
                                             MeasurementsTable.windSpeed.avg to "avgWindspeed",
                                             MeasurementsTable.windSpeed.max to "maxWindspeed",
                                             MeasurementsTable.visibility.min to "minVisibility",
                                             MeasurementsTable.visibility.avg to "avgVisibility",
                                             MeasurementsTable.visibility.max to "maxVisibility")

private data class NumberInterval(val start: Int, val end: Int) {
    val difference get() = end - start
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

private fun toSummarizedData(row: ResultRow): SummarizedYearData {
    return SummarizedYearData(firstDay = LocalDate(row[MeasurementsTable.year], 1, 1),
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

data class SummarizedYearData(
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