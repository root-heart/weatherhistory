package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import org.joda.time.DateTime
import rootheart.codes.common.collections.avgDecimal
import rootheart.codes.common.collections.maxDecimal
import rootheart.codes.common.collections.minDecimal
import rootheart.codes.common.collections.sumDecimal
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import java.math.BigDecimal
import java.math.RoundingMode


private val SIXTY = BigDecimal.valueOf(60)

@DelicateCoroutinesApi
object Summarizer {
    fun summarizeSummarizedRecords(
        station: Station,
        interval: DateInterval,
        measurements: Collection<SummarizedMeasurement>
    ) = SummarizedMeasurement(
        station = station,
        interval = interval,
        countCloudCoverage0 = measurements.sumOf { it.countCloudCoverage0 },
        countCloudCoverage1 = measurements.sumOf { it.countCloudCoverage1 },
        countCloudCoverage2 = measurements.sumOf { it.countCloudCoverage2 },
        countCloudCoverage3 = measurements.sumOf { it.countCloudCoverage3 },
        countCloudCoverage4 = measurements.sumOf { it.countCloudCoverage4 },
        countCloudCoverage5 = measurements.sumOf { it.countCloudCoverage5 },
        countCloudCoverage6 = measurements.sumOf { it.countCloudCoverage6 },
        countCloudCoverage7 = measurements.sumOf { it.countCloudCoverage7 },
        countCloudCoverage8 = measurements.sumOf { it.countCloudCoverage8 },
        countCloudCoverageNotVisible = measurements.sumOf { it.countCloudCoverageNotVisible },
        countCloudCoverageNotMeasured = measurements.sumOf { it.countCloudCoverageNotMeasured },

        minDewPointTemperatureCentigrade = measurements.minDecimal { it.minDewPointTemperatureCentigrade },
        avgDewPointTemperatureCentigrade = measurements.avgDecimal { it.avgDewPointTemperatureCentigrade },
        maxDewPointTemperatureCentigrade = measurements.maxDecimal { it.maxDewPointTemperatureCentigrade },

        minAirTemperatureCentigrade = measurements.minDecimal { it.minAirTemperatureCentigrade },
        avgAirTemperatureCentigrade = measurements.avgDecimal { it.avgAirTemperatureCentigrade },
        maxAirTemperatureCentigrade = measurements.maxDecimal { it.maxAirTemperatureCentigrade },

        minHumidityPercent = measurements.minDecimal { it.minHumidityPercent },
        avgHumidityPercent = measurements.avgDecimal { it.avgHumidityPercent },
        maxHumidityPercent = measurements.maxDecimal { it.maxHumidityPercent },

        minAirPressureHectopascals = measurements.minDecimal { it.minAirPressureHectopascals },
        avgAirPressureHectopascals = measurements.avgDecimal { it.avgAirPressureHectopascals },
        maxAirPressureHectopascals = measurements.maxDecimal { it.maxAirPressureHectopascals },

        sumSunshineDurationHours = measurements.sumDecimal { it.sumSunshineDurationHours },

        details = ""
    )

    fun summarize(
        station: Station,
        intervalGetter: (DateTime) -> DateInterval,
        measurements: Collection<HourlyMeasurement>
    ): List<SummarizedMeasurement> {
        // TODO we are assuming that all measurements belong to the same station...
        val grouped = measurements.groupBy { intervalGetter(it.measurementTime) }
        return grouped.map { (group, measurements) ->
            summarize(station, group, measurements)
        }
    }

    private fun summarize(station: Station, interval: DateInterval, measurements: Collection<HourlyMeasurement>) =
        SummarizedMeasurement(station = station,
            interval = interval,
            countCloudCoverage0 = measurements.count { it.cloudCoverage == 0 },
            countCloudCoverage1 = measurements.count { it.cloudCoverage == 1 },
            countCloudCoverage2 = measurements.count { it.cloudCoverage == 2 },
            countCloudCoverage3 = measurements.count { it.cloudCoverage == 3 },
            countCloudCoverage4 = measurements.count { it.cloudCoverage == 4 },
            countCloudCoverage5 = measurements.count { it.cloudCoverage == 5 },
            countCloudCoverage6 = measurements.count { it.cloudCoverage == 6 },
            countCloudCoverage7 = measurements.count { it.cloudCoverage == 7 },
            countCloudCoverage8 = measurements.count { it.cloudCoverage == 8 },
            countCloudCoverageNotVisible = measurements.count { it.cloudCoverage == -1 },
            countCloudCoverageNotMeasured = measurements.count { it.cloudCoverage == null },

            minDewPointTemperatureCentigrade = measurements.minDecimal { it.dewPointTemperatureCentigrade },
            avgDewPointTemperatureCentigrade = measurements.avgDecimal { it.dewPointTemperatureCentigrade },
            maxDewPointTemperatureCentigrade = measurements.maxDecimal { it.dewPointTemperatureCentigrade },

            minAirTemperatureCentigrade = measurements.minDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },
            avgAirTemperatureCentigrade = measurements.avgDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },
            maxAirTemperatureCentigrade = measurements.maxDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },

            minHumidityPercent = measurements.minDecimal { it.relativeHumidityPercent },
            avgHumidityPercent = measurements.avgDecimal { it.relativeHumidityPercent },
            maxHumidityPercent = measurements.maxDecimal { it.relativeHumidityPercent },

            minAirPressureHectopascals = measurements.minDecimal { it.airPressureHectopascals },
            avgAirPressureHectopascals = measurements.avgDecimal { it.airPressureHectopascals },
            maxAirPressureHectopascals = measurements.maxDecimal { it.airPressureHectopascals },

            sumSunshineDurationHours = measurements.sumDecimal { it.sunshineDurationMinutes }
                ?.divide(SIXTY, RoundingMode.HALF_UP),

            maxWindSpeedMetersPerSecond = measurements.maxDecimal { it.maxWindSpeedMetersPerSecond },
            avgWindSpeedMetersPerSecond = measurements.avgDecimal { it.windSpeedMetersPerSecond }
        )
}