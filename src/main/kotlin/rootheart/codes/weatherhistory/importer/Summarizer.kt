package rootheart.codes.weatherhistory.importer

import org.joda.time.LocalDateTime
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal
import java.math.RoundingMode

object Summarizer {
    fun summarize(
        stationId: StationId,
        hourlyRecords: Collection<HourlyRecord>,
        groupingFunction: (LocalDateTime) -> DateInterval
    ): Collection<SummarizedMeasurement> {
        val groupedHourlyRecords = hourlyRecords.groupBy { groupingFunction(it.measurementTime) }
        val summarizedMeasurements = HashMap<DateInterval, SummarizedMeasurement>()
        for ((dateInterval, records) in groupedHourlyRecords) {
            val summary = summarizedMeasurements.getOrPut(dateInterval) {
                SummarizedMeasurement(stationId, dateInterval)
            }
            summary.countCloudCoverage0 = records.count { it.cloudCoverage == 0 }
            summary.countCloudCoverage1 = records.count { it.cloudCoverage == 1 }
            summary.countCloudCoverage2 = records.count { it.cloudCoverage == 2 }
            summary.countCloudCoverage3 = records.count { it.cloudCoverage == 3 }
            summary.countCloudCoverage4 = records.count { it.cloudCoverage == 4 }
            summary.countCloudCoverage5 = records.count { it.cloudCoverage == 5 }
            summary.countCloudCoverage6 = records.count { it.cloudCoverage == 6 }
            summary.countCloudCoverage7 = records.count { it.cloudCoverage == 7 }
            summary.countCloudCoverage8 = records.count { it.cloudCoverage == 8 }
            summary.countCloudCoverageNotVisible = records.count { it.cloudCoverage == -1 }
            summary.countCloudCoverageNotMeasured = records.count { it.cloudCoverage == null }
            summary.minDewPointTemperatureCentigrade =
                records.minOf { it.dewPointTemperatureCentigrade ?: BigDecimal.ZERO }
            summary.avgDewPointTemperatureCentigrade =
                records.sumOf { it.dewPointTemperatureCentigrade ?: BigDecimal.ZERO }
                    .divide(BigDecimal.valueOf(records.size.toLong()), RoundingMode.HALF_UP)
            summary.avgDewPointTemperatureCentigrade =
                records.maxOf { it.dewPointTemperatureCentigrade ?: BigDecimal.ZERO }
        }
        return summarizedMeasurements.values
    }

}