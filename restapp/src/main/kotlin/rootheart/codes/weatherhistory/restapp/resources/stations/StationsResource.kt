package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.summarized.MonthlySummary
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurementsTable
import rootheart.codes.weatherhistory.database.summarized.YearlySummary

@Resource("stations")
class Stations

@Resource("{id}")
class StationById(val stations: Stations, val id: Long)


fun Routing.stationsResource() {
    get<Stations> {
        call.respond(StationDao.findAll().sortedBy { it.federalState + it.name })
    }

    get<StationById> { request ->
        call.respond(StationDao.findById(request.id.toLong()) ?: HttpStatusCode.NotFound)

    }
}

//fun Routing.stationsResource() {
//    route("stations") {
//        get { call.respond(StationDao.findAll().sortedBy { it.federalState + it.name }) }
//
//        route("{stationId}") {
//            get {
//                val stationId = requiredPathParam("stationId") { it.toLong() }
//                call.respond(StationDao.findById(stationId) ?: HttpStatusCode.NotFound)
//            }
//
//            get("summary/{years?}/{months?}") {
//                val stationId = requiredPathParam("stationId") { it.toLong() }
//                val years = requiredPathParam("years") { toInterval(it) }
//                val months = optPathParam("months") { toIntervalList(it) }
//
//                val data = transaction {
//                    val firstAndLastDay = with(DailyMeasurementTable) {
//                        var condition = this.stationId.eq(stationId)
//                                .and(year.greaterEq(years.start))
//                                .and(year.lessEq(years.end))
//                        if (months != null) {
//                            val monthsList = months.map { it.elements() }.flatten()
//                            condition = condition.and(month.inList(monthsList))
//                        }
//
//                        slice(date.min(), date.max())
//                                .select(condition)
//                                .map {
//                                    val min = it[date.min()]
//                                    val max = it[date.max()]
//                                    if (min != null && max != null) {
//                                        return@map FirstAndLastDay(min.toLocalDate(), max.toLocalDate())
//                                    } else {
//                                        return@map null
//                                    }
//                                }
//                                .first() ?: return@transaction ArrayList<MeasurementJson>()
//                    }
//
//                    if (firstAndLastDay.monthsCount <= 12) {
//                        val details = with(DailyMeasurementTable) {
//                            var condition = this.stationId.eq(stationId)
//                                    .and(year.greaterEq(years.start))
//                                    .and(year.lessEq(years.end))
//                            if (months != null) {
//                                val monthsList = months.map { it.elements() }.flatten()
//                                condition = condition.and(month.inList(monthsList))
//                            }
//
//                            select(condition).map(::toEntity)
//                        }
//
//                        val summary = details.summarizeDaily()
//
//                        return@transaction mapOf(
//                                "summary" to summary,
//                                "details" to details.sortedBy { it.dateInUtcMillis },
//                                "resolution" to "day")
//                    } else if (firstAndLastDay.yearsCount <= 2) {
//                        val details = with(SummarizedMeasurementsTable) {
//                            var condition = this.stationId.eq(stationId)
//                                    .and(year.greaterEq(years.start))
//                                    .and(year.lessEq(years.end))
//                            condition = if (months != null) {
//                                val monthsList = months.map { it.elements() }.flatten()
//                                condition.and(month.inList(monthsList))
//                            } else {
//                                condition.and(month.isNotNull())
//                            }
//
//                            select(condition).map(::toMonthlySummary)
//                        }
//
//                        val summary = details.summarizeMonthly()
//
//                        return@transaction mapOf("summary" to summary,
//                                                 "details" to details.sortedBy { LocalDate(it.year, it.month, 1) },
//                                                 "resolution" to "day")
//                    } else if (months != null) {
//                        val details = with(SummarizedMeasurementsTable) {
//                            val monthsList = months.map { it.elements() }.flatten()
//                            val condition = this.stationId.eq(stationId)
//                                    .and(year.greaterEq(years.start))
//                                    .and(year.lessEq(years.end))
//                                    .and(month.inList(monthsList))
//
//                            select(condition).groupBy { it[year] }
//                                    .mapValues { it.value.map(::toMonthlySummary) }
//                                    .mapValues { it.value.summarizeMonthly() }
//                                    .map { YearlySummary(it.key, it.value) }
//                        }
//
//                        val summary = details.summarizeYearly()
//
//                        return@transaction mapOf("summary" to summary,
//                                                 "details" to details.sortedBy { it.year },
//                                                 "resolution" to "day")
//                    } else {
//                        val details = with(SummarizedMeasurementsTable) {
//                            val condition = this.stationId.eq(stationId)
//                                    .and(year.greaterEq(years.start))
//                                    .and(year.lessEq(years.end))
//                                    .and(month.isNull())
//                            select(condition).map(::toYearlySummary)
//                        }
//                        val summary = details.summarizeYearly()
//                        return@transaction mapOf("summary" to summary,
//                                                 "details" to details.sortedBy { it.year },
//                                                 "resolution" to "day")
//                    }
//                }
//
//                call.respond(data)
//            }
//        }
//    }
//}

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

fun SummarizedMeasurementsTable.toMonthlySummary(row: ResultRow) =
    MonthlySummary(row[year],
                   row[month] ?: 1,
                   SummarizedMeasurement(stationId = row[stationId].value,
                                         airTemperatureCentigrade = airTemperatureCentigrade.toEntity(row),
                                         dewPointTemperatureCentigrade = dewPointTemperatureCentigrade.toEntity(
                                                 row),
                                         humidityPercent = humidityPercent.toEntity(row),
                                         airPressureHectopascals = airPressureHectopascals.toEntity(row),
                                         sunshineMinutes = sunshineMinutes.toEntity(row),
                                         rainfallMillimeters = rainfallMillimeters.toEntity(row),
                                         snowfallMillimeters = snowfallMillimeters.toEntity(row),
                                         windSpeedMetersPerSecond = windSpeedMetersPerSecond.toEntity(row),
                                         visibilityMeters = visibilityMeters.toEntity(row),

                                         cloudCoverageHistogram = row[cloudCoverageHistogram],
                                         detailedCloudCoverage = row[detailedCloudCoverage]))

fun SummarizedMeasurementsTable.toYearlySummary(row: ResultRow) =
    YearlySummary(row[year],
                  SummarizedMeasurement(stationId = row[stationId].value,
                                        airTemperatureCentigrade = airTemperatureCentigrade.toEntity(row),
                                        dewPointTemperatureCentigrade = dewPointTemperatureCentigrade.toEntity(
                                                row),
                                        humidityPercent = humidityPercent.toEntity(row),
                                        airPressureHectopascals = airPressureHectopascals.toEntity(row),
                                        sunshineMinutes = sunshineMinutes.toEntity(row),
                                        rainfallMillimeters = rainfallMillimeters.toEntity(row),
                                        snowfallMillimeters = snowfallMillimeters.toEntity(row),
                                        windSpeedMetersPerSecond = windSpeedMetersPerSecond.toEntity(row),
                                        visibilityMeters = visibilityMeters.toEntity(row),

                                        cloudCoverageHistogram = row[cloudCoverageHistogram],
                                        detailedCloudCoverage = row[detailedCloudCoverage]))
