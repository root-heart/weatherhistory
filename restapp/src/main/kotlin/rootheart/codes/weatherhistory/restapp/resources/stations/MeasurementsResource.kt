package rootheart.codes.weatherhistory.restapp.resources.stations

import io.ktor.http.HttpStatusCode
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
import org.jetbrains.exposed.sql.Column
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
import rootheart.codes.weatherhistory.database.daily.DailySumColums
import rootheart.codes.weatherhistory.database.daily.HistogramData

@Resource("{measurement}/{year}")
class MeasurementResource(val stationById: StationById, val measurement: String, val year: Int)

@Resource("{measurement}/histogram/{year}")
class HistogramResource(val stationById: StationById, val measurement: String, val year: Int)

@Resource("sunshine-cloud-coverage/{year}")
class SunshineCloudCoverage(val stationById: StationById, val year: Int)

private val minAvgMaxMeasurements = with(DailyMeasurementTable) {
    mapOf("air-temperature" to airTemperatureCentigrade,
          "dew-point-temperature" to dewPointTemperatureCentigrade,
          "humidity" to humidityPercent,
          "air-pressure" to airPressureHectopascals,
          "visibility" to visibilityMeters)

}

private val sumMeasurements = with(DailyMeasurementTable) {
    mapOf("sunshine" to sunshineMinutes,
          "rain" to rainfallMillimeters,
          "snow" to snowfallMillimeters)
}

private val histogramMeasurements = with(DailyMeasurementTable) {
    mapOf("air-temperature" to airTemperatureCentigrade.details,
          "sunshine" to sunshineMinutes.details,
//          "cloud-coverage" to detailedCloudCoverage
    )
}

fun Routing.measurementsResource() {
    get<MeasurementResource> { request ->
        if (minAvgMaxMeasurements.containsKey(request.measurement)) {
            val columns = minAvgMaxMeasurements[request.measurement]
            call.respond(DailyMeasurementTable.fetchMinAvgMaxData(columns!!, request.stationById.id, request.year))
        } else if (sumMeasurements.containsKey(request.measurement)) {
            val columns = sumMeasurements[request.measurement]
            call.respond(DailyMeasurementTable.fetchSumData(columns!!, request.stationById.id, request.year))
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    get<HistogramResource> { request ->
        val column = histogramMeasurements[request.measurement]
        if (column != null) {
            call.respond(DailyMeasurementTable.fetchHistogramData(column, request.stationById.id, request.year))
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
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
