package rootheart.codes.weatherhistory.restapp.resources

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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementDao
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementTable
import rootheart.codes.weatherhistory.database.daily.summarizeDaily
import rootheart.codes.weatherhistory.database.summarized.MonthlySummary
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurementsTable
import rootheart.codes.weatherhistory.database.summarized.YearlySummary
import rootheart.codes.weatherhistory.database.summarized.summarizeYearly
import rootheart.codes.weatherhistory.database.summarized.summarizeMonthly
import rootheart.codes.weatherhistory.restapp.*

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
                    val firstAndLastDay = DailyMeasurementDao.getFirstAndLastDay(stationId, years.start, years.end)

                    if (firstAndLastDay.monthsCount <= 12) {
                        val details = with(DailyMeasurementTable) {
                            var condition = this.stationId.eq(stationId)
                                    .and(year.greaterEq(years.start))
                                    .and(year.lessEq(years.end))
                            if (months != null) {
                                val monthsList = months.map { it.elements() }.flatten()
                                condition = condition.and(month.inList(monthsList))
                            }

                            select(condition).map { toEntity(it) }
                        }

                        val summary = details.summarizeDaily()

                        return@transaction mapOf(
                                "summary" to summary,
                                "details" to details.sortedBy { it.date },
                                "resolution" to "day")
                    } else if (firstAndLastDay.yearsCount <= 2) {
                        val details = with(SummarizedMeasurementsTable) {
                            var condition = this.stationId.eq(stationId)
                                    .and(year.greaterEq(years.start))
                                    .and(year.lessEq(years.end))
                            condition = if (months != null) {
                                val monthsList = months.map { it.elements() }.flatten()
                                condition.and(month.inList(monthsList))
                            } else {
                                condition.and(month.isNotNull())
                            }

                            select(condition).map(::toMonthlySummary)
                        }

                        val summary = details.summarizeMonthly()

                        return@transaction mapOf("summary" to summary,
                                                 "details" to details.sortedBy { LocalDate(it.year, it.month, 1) },
                                                 "resolution" to "day")
                    } else if (months != null) {
                        val details = with(SummarizedMeasurementsTable) {
                            val monthsList = months.map { it.elements() }.flatten()
                            val condition = this.stationId.eq(stationId)
                                    .and(year.greaterEq(years.start))
                                    .and(year.lessEq(years.end))
                                    .and(month.inList(monthsList))

                            select(condition).groupBy { it[year] }
                                    .mapValues { it.value.map(::toMonthlySummary) }
                                    .mapValues { it.value.summarizeMonthly() }
                                    .map { YearlySummary(it.key, it.value) }
                        }

                        val summary = details.summarizeYearly()

                        return@transaction mapOf("summary" to summary,
                                                 "details" to details.sortedBy { it.year },
                                                 "resolution" to "day")
                    } else {
                        val details = with(SummarizedMeasurementsTable) {
                            val condition = this.stationId.eq(stationId)
                                    .and(year.greaterEq(years.start))
                                    .and(year.lessEq(years.end))
                                    .and(month.isNull())
                            select(condition).map(::toYearlySummary)
                        }
                        val summary = details.summarizeYearly()
                        return@transaction mapOf("summary" to summary,
                                                 "details" to details.sortedBy { it.year },
                                                 "resolution" to "day")
                    }
                }

                call.respond(data)
            }
        }
    }
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
