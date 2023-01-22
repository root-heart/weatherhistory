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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
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
                    call.respond(mapOf("summary" to data,
                                       "details" to data,
                                       "resolution" to if (resolution == Interval.YEAR) "year" else "month"))
                } else if (yearDifference == 0) {
                    val monthsList = months.map { it.elements() }.flatten()
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
                    call.respond(mapOf("summary" to data,
                                       "details" to data,
                                       "resolution" to "month"))
                } else {
                    val monthsList = months.map { it.elements() }.flatten()
                    if (columns == MeasurementsTable.summaryColumns) {
                        val data = transaction {
                            columns.fields
                                    .select(MeasurementsTable.stationId.eq(stationId)
                                                    .and(MeasurementsTable.interval.eq(Interval.MONTH))
                                                    .and(MeasurementsTable.year.greaterEq(years.start))
                                                    .and(MeasurementsTable.year.lessEq(years.end))
                                                    .and(MeasurementsTable.month.inList(monthsList)))
                                    .orderBy(MeasurementsTable.year to SortOrder.ASC)
                                    .map(columns::toSummarizedData)
                        }
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
                        call.respond(mapOf("summary" to list,
                                           "details" to list,
                                           "resolution" to "year"))
                    }
                }
            }
        }
    }
}

private class YearAccumulator(val summary: SummarizedYearData)

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

private fun MeasurementColumns.toMap(row: ResultRow): Map<String, Any?> {
    val map = HashMap<String, Any?>()
    map["firstDay"] =
            LocalDate(row[MeasurementsTable.year], row[MeasurementsTable.month] ?: 1, row[MeasurementsTable.day] ?: 1)
    columns.forEach { map[it.second] = row[it.first] }
    return map
}

private fun MeasurementColumns.toSummarizedData(row: ResultRow): SummarizedYearData {
    return SummarizedYearData(
            firstDay = LocalDate(row[MeasurementsTable.year], 1, 1),
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
            maxVisibility = row[MeasurementsTable.visibility.max],
    )
}

data class DayClassHistogram(val icyDays: Int, val frostyDays: Int, val vegetationDays: Int, val summerDays: Int,
                             val hotDays: Int, val desertDays: Int, val tropicNights: Int)


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