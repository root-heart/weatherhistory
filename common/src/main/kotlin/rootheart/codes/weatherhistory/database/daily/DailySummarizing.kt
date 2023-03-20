package rootheart.codes.weatherhistory.database.daily

import org.joda.time.LocalDate
import rootheart.codes.common.collections.nullsafeAvgDecimal
import rootheart.codes.weatherhistory.database.summarized.MonthlySummary
import rootheart.codes.weatherhistory.database.summarized.SummarizedAvgMax
import rootheart.codes.weatherhistory.database.summarized.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.summarized.SummarizedMinAvgMax
import rootheart.codes.weatherhistory.database.summarized.SummarizedSum


fun groupDailyByMonth(measurements: Collection<DailyMeasurementEntity>): Collection<MonthlySummary> {
    return measurements.groupBy { LocalDate(it.date.year, it.date.monthOfYear, 1) }
            .mapValues {
                MonthlySummary(year = it.key.year,
                               month = it.key.monthOfYear,
                               measurements = it.value.summarizeDaily())
            }
            .values
}

fun Collection<DailyMeasurementEntity>.summarizeDaily(): SummarizedMeasurement {
    val cloudCoverageHistogram = Array(10) { 0 }
    for (m in this) {
        m.cloudCoverageHistogram?.forEachIndexed { index, coverage -> cloudCoverageHistogram[index] += coverage }
    }
    return SummarizedMeasurement(stationId = first().stationId,

                                 airTemperatureCentigrade = summarizeDailyMinAvgMax { it.airTemperatureCentigrade },
                                 dewPointTemperatureCentigrade = summarizeDailyMinAvgMax { it.dewPointTemperatureCentigrade },
                                 humidityPercent = summarizeDailyMinAvgMax { it.humidityPercent },
                                 airPressureHectopascals = summarizeDailyMinAvgMax { it.airPressureHectopascals },
                                 visibilityMeters = summarizeDailyMinAvgMax { it.visibilityMeters },
                                 windSpeedMetersPerSecond = summarizeDailyAvgMax { it.windSpeedMetersPerSecond },
                                 sunshineMinutes = summarizeDailySums { it.sunshineMinutes },
                                 rainfallMillimeters = summarizeDailySums { it.rainfallMillimeters },
                                 snowfallMillimeters = summarizeDailySums { it.snowfallMillimeters },

                                 cloudCoverageHistogram = cloudCoverageHistogram,
                                 detailedCloudCoverage = Array(0) { 0 },
                                 detailedWindDirectionDegrees = Array(0) { null })
}


fun Collection<DailyMeasurementEntity>.summarizeDailyMinAvgMax(
        selector: (DailyMeasurementEntity) -> DailyMinAvgMax): SummarizedMinAvgMax {
    val minMeasurement = filter { selector(it).min != null }.minByOrNull { selector(it).min!! }
    val maxMeasurement = filter { selector(it).max != null }.maxByOrNull { selector(it).max!! }
    return SummarizedMinAvgMax(
            min = minMeasurement?.let(selector)?.min,
            minDate = minMeasurement?.date,
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.let(selector)?.max,
            maxDate = maxMeasurement?.date)
}

fun Collection<DailyMeasurementEntity>.summarizeDailyAvgMax(
        selector: (DailyMeasurementEntity) -> DailyAvgMax): SummarizedAvgMax {
    val maxMeasurement = filter { selector(it).max != null }.maxByOrNull { selector(it).max!! }
    return SummarizedAvgMax(
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.let(selector)?.max,
            maxDate = maxMeasurement?.date)
}

fun Collection<DailyMeasurementEntity>.summarizeDailySums(
        selector: (DailyMeasurementEntity) -> DailySum): SummarizedSum {
    val notNullSums = filter { selector(it).sum != null }
    val minMeasurement = notNullSums.maxByOrNull { selector(it).sum!! }
    val maxMeasurement = notNullSums.maxByOrNull { selector(it).sum!! }
    return SummarizedSum(
            min = minMeasurement?.let(selector)?.sum,
            minDate = minMeasurement?.date,
            max = maxMeasurement?.let(selector)?.sum,
            maxDate = maxMeasurement?.date,
            sum = notNullSums.sumOf { selector(it).sum!! }
    )
}
