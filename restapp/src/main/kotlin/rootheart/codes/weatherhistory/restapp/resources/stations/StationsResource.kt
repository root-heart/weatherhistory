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
import rootheart.codes.common.collections.*
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

                    var measurements = MeasurementsTable.slice(dataColumns)
                            .select(condition.and(MeasurementsTable.interval.eq(resolution)))
                            .map(::toJson)
                            .sortedBy { it.firstDay }

                    if (customAggregation) {
                        measurements = measurements.groupBy { it.firstDay.year }
                                .mapValues { (year, measurements) ->
                                    val cloudCoverages = measurements.map { it.cloudCoverage }
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

                                    MeasurementJson(
                                            firstDay = LocalDate(year, 1, 1),
                                            temperature = measurements.minAvgMaxDecimals { it.temperature },
                                            dewPointTemperature = measurements.minAvgMaxDecimals { it.dewPointTemperature },
                                            humidity = measurements.minAvgMaxDecimals { it.humidity },
                                            airPressure = measurements.minAvgMaxDecimals { it.airPressure },

                                            cloudCoverage = sumCloudCoverages,
                                            sunshine = measurements.minMaxSumInts { it.sunshine },
                                            rainfall = measurements.minMaxSumDecimals { it.rainfall },
                                            snowfall = measurements.minMaxSumDecimals { it.snowfall },
                                            windSpeed = measurements.avgMaxDecimals { it.windSpeed },
                                            visibility = measurements.minAvgMaxInts { it.visibility })
                                }
                                .values
                                .sortedBy { it.firstDay }
                    }

                    val cloudCoverages = measurements.map { it.cloudCoverage }
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
                    val summarizedMeasurements = SummarizedMeasurementJson(
                            temperature = measurements.minAvgMaxDecimals { it.temperature },
                            dewPointTemperature = measurements.minAvgMaxDecimals { it.dewPointTemperature },
                            humidity = measurements.minAvgMaxDecimals { it.humidity },
                            airPressure = measurements.minAvgMaxDecimals { it.airPressure },

                            cloudCoverage = sumCloudCoverages,
                            sunshine = measurements.minMaxSumInts { it.sunshine },

                            rainfall = measurements.minMaxSumDecimals { it.rainfall },
                            snowfall = measurements.minMaxSumDecimals { it.snowfall },
                            windSpeed = measurements.avgMaxDecimals { it.windSpeed },
                            visibility = measurements.minAvgMaxInts { it.visibility })

                    return@transaction mapOf("summary" to summarizedMeasurements,
                                             "details" to measurements,
                                             "resolution" to if (resolution == Interval.DAY) "day" else if (resolution == Interval.MONTH) "month" else "year")
                }

                call.respond(data)
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
                                 MeasurementsTable.temperatures.minDay,
                                 MeasurementsTable.temperatures.avg,
                                 MeasurementsTable.temperatures.max,
                                 MeasurementsTable.temperatures.maxDay,
                                 MeasurementsTable.dewPointTemperatures.min,
                                 MeasurementsTable.dewPointTemperatures.minDay,
                                 MeasurementsTable.dewPointTemperatures.avg,
                                 MeasurementsTable.dewPointTemperatures.max,
                                 MeasurementsTable.dewPointTemperatures.maxDay,
                                 MeasurementsTable.humidity.min,
                                 MeasurementsTable.humidity.minDay,
                                 MeasurementsTable.humidity.avg,
                                 MeasurementsTable.humidity.max,
                                 MeasurementsTable.humidity.maxDay,
                                 MeasurementsTable.airPressure.min,
                                 MeasurementsTable.airPressure.minDay,
                                 MeasurementsTable.airPressure.avg,
                                 MeasurementsTable.airPressure.max,
                                 MeasurementsTable.airPressure.maxDay,
                                 MeasurementsTable.cloudCoverage.histogram,
                                 MeasurementsTable.sunshine.min,
                                 MeasurementsTable.sunshine.minDay,
                                 MeasurementsTable.sunshine.max,
                                 MeasurementsTable.sunshine.maxDay,
                                 MeasurementsTable.sunshine.sum,
                                 MeasurementsTable.sunshine.details,
                                 MeasurementsTable.rainfall.min,
                                 MeasurementsTable.rainfall.minDay,
                                 MeasurementsTable.rainfall.max,
                                 MeasurementsTable.rainfall.maxDay,
                                 MeasurementsTable.rainfall.sum,
                                 MeasurementsTable.rainfall.details,
                                 MeasurementsTable.snowfall.min,
                                 MeasurementsTable.snowfall.minDay,
                                 MeasurementsTable.snowfall.max,
                                 MeasurementsTable.snowfall.maxDay,
                                 MeasurementsTable.snowfall.sum,
                                 MeasurementsTable.snowfall.details,
                                 MeasurementsTable.windSpeed.avg,
                                 MeasurementsTable.windSpeed.max,
                                 MeasurementsTable.windSpeed.maxDay,
                                 MeasurementsTable.visibility.min,
                                 MeasurementsTable.visibility.minDay,
                                 MeasurementsTable.visibility.avg,
                                 MeasurementsTable.visibility.max,
                                 MeasurementsTable.visibility.maxDay)

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

private fun toJson(row: ResultRow): MeasurementJson = with(MeasurementsTable) {
    MeasurementJson(
            firstDay = row[firstDay].toLocalDate(),
            temperature = temperatures.toJson(row),
            dewPointTemperature = dewPointTemperatures.toJson(row),
            humidity = humidity.toJson(row),
            airPressure = airPressure.toJson(row),
            cloudCoverage = row[cloudCoverage.histogram].toList(),
            sunshine = sunshine.toJson(row),
            rainfall = rainfall.toJson(row),
            snowfall = snowfall.toJson(row),
            windSpeed = windSpeed.toJson(row),
            visibility = visibility.toJson(row))
}


private fun <T : Number> MinAvgMaxDetailsColumns<T>.toJson(row: ResultRow) =
    MinAvgMax(min = row[min],
              minDay = row[minDay]?.toLocalDate(),
              avg = row[avg],
              max = row[max],
              maxDay = row[maxDay]?.toLocalDate())

private fun <T : Number> AvgMaxDetailsColumns<T>.toJson(row: ResultRow) =
    AvgMax(avg = row[avg],
           max = row[max],
           maxDay = row[maxDay]?.toLocalDate())

private fun <N : Number> MinMaxSumDetailsColumns<N>.toJson(row: ResultRow) =
    MinMaxSumDetails(min = row[min],
                     minDay = row[minDay]?.toLocalDate(),
                     max = row[max],
                     maxDay = row[maxDay]?.toLocalDate(),
                     sum = row[sum],
                     details = row[details]
    )


data class SummarizedMeasurementJson(
        val temperature: MinAvgMax<BigDecimal>?,
        val dewPointTemperature: MinAvgMax<BigDecimal>?,
        val humidity: MinAvgMax<BigDecimal>?,
        val airPressure: MinAvgMax<BigDecimal>?,
        val cloudCoverage: List<Int>,
        val sunshine: MinMaxSumDetails<Int>?,
        val rainfall: MinMaxSumDetails<BigDecimal>?,
        val snowfall: MinMaxSumDetails<BigDecimal>?,
        val windSpeed: AvgMax<BigDecimal>?,
        val visibility: MinAvgMax<Int>?,
)
