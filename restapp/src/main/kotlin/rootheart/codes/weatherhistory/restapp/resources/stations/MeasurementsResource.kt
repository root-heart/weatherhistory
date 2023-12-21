package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.request.receiveStream
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.min
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementTable
import rootheart.codes.weatherhistory.database.daily.DailyMinAvgMaxColumns
import rootheart.codes.weatherhistory.database.daily.HistogramData

@Resource("cloud-coverage/{year}")
class CloudCoverage(val stationById: StationById, val year: Int)

@Resource("sunshine-duration/{year}")
class SunshineDuration(val stationById: StationById, val year: Int)

@Resource("sunshine-cloud-coverage/{year}")
class SunshineCloudCoverage(val stationById: StationById, val year: Int)

@Resource("air-temperature/{year}")
class AirTemperature(val stationById: StationById, val year: Int)

fun Routing.measurementsResource() {
    get<AirTemperature> { request ->
        val data = DailyMeasurementTable.fetchMinAvgMaxData(DailyMeasurementTable.airTemperatureCentigrade,
                                                            request.stationById.id,
                                                            request.year)
        call.respond(data)
    }

    get<CloudCoverage> { request ->
        val histogramData = DailyMeasurementTable.fetchHistogramData(DailyMeasurementTable.detailedCloudCoverage,
                                                                     request.stationById.id,
                                                                     request.year)
        call.respond(histogramData)
    }

    get<SunshineDuration> { request ->
        val histogramData = DailyMeasurementTable.fetchHistogramData(DailyMeasurementTable.sunshineMinutes.details,
                                                                     request.stationById.id,
                                                                     request.year)
        call.respond(histogramData)
    }

    get<SunshineCloudCoverage> { m ->
        val histogramData = transaction {
            addLogger(StdOutSqlLogger)
            with(DailyMeasurementTable) {
                slice(date, detailedCloudCoverage, sunshineMinutes.details)
                        .select(stationId.eq(m.stationById.id)
                                        .and(year.eq(m.year))
                                        .and(detailedCloudCoverage.isNotNull())
                                        .and(sunshineMinutes.details.isNotNull()))
                        .map {
                            val sunshineCloudCoverage = Array<BigDecimal?>(24) { null }
                            for (i in 0..23) {
                                val cloudCoverage = it[detailedCloudCoverage]?.get(i) ?: 0
                                val sunshineDuration = it[sunshineMinutes.details]?.get(i) ?: BigDecimal.ZERO
                                val cloudCoverageDecimal =
                                    BigDecimal(cloudCoverage).divide(BigDecimal(8), 3, RoundingMode.UNNECESSARY)
                                sunshineCloudCoverage[i] = sunshineDuration * (BigDecimal.ONE - cloudCoverageDecimal)
                            }
                            HistogramData(it[date].toDate(), sunshineCloudCoverage)
                        }
            }
        }
        call.respond(histogramData)
    }
}
