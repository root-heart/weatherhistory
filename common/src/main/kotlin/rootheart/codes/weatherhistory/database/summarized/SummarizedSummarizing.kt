package rootheart.codes.weatherhistory.database.summarized

import org.joda.time.LocalDate
import rootheart.codes.common.collections.nullsafeAvgDecimal
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementEntity

fun groupMonthlyByYear(measurements: Collection<MonthlySummary>): Collection<YearlySummary> {
    return measurements
            .groupBy { it.year }
            .mapValues { it.value.summarizeMonthly() }
            .mapValues { YearlySummary(it.key, it.value) }
            .values
}

fun Collection<MonthlySummary>.summarizeMonthly(): SummarizedMeasurement {
    val summarizedHistogram = Array(10) { 0 }
    val detailedCloudCoverage = Array(size) { Array(10) { 0 } }
    map(MonthlySummary::measurements)
            .map { it.cloudCoverageHistogram }
            .forEachIndexed { day, detailedHistogram ->
                if (detailedHistogram != null) {
                    detailedCloudCoverage[day] = detailedHistogram
                    detailedHistogram.forEachIndexed { coverage, count -> summarizedHistogram[coverage] += count }
                }
            }

    return SummarizedMeasurement(stationId = first().measurements.stationId,

                                 airTemperatureCentigrade = summarizeMonthlyMinAvgMax { it.measurements.airTemperatureCentigrade },
                                 dewPointTemperatureCentigrade = summarizeMonthlyMinAvgMax { it.measurements.dewPointTemperatureCentigrade },
                                 humidityPercent = summarizeMonthlyMinAvgMax { it.measurements.humidityPercent },
                                 airPressureHectopascals = summarizeMonthlyMinAvgMax { it.measurements.airPressureHectopascals },
                                 visibilityMeters = summarizeMonthlyMinAvgMax { it.measurements.visibilityMeters },
                                 windSpeedMetersPerSecond = summarizeMonthlyAvgMax { it.measurements.windSpeedMetersPerSecond },

                                 cloudCoverageHistogram = summarizedHistogram,
                                 detailedCloudCoverage = detailedCloudCoverage,

                                 sunshineMinutes = summarizeMonthlySums { it.measurements.sunshineMinutes },
                                 rainfallMillimeters = summarizeMonthlySums { it.measurements.rainfallMillimeters },
                                 snowfallMillimeters = summarizeMonthlySums { it.measurements.snowfallMillimeters },

                                 detailedWindDirectionDegrees = Array(0) { null })
}

fun Collection<YearlySummary>.summarizeYearly(): SummarizedMeasurement {
    val summarizedHistogram = Array(10) { 0 }
    val detailedCloudCoverage = Array(size) { Array(10) { 0 } }
    map(YearlySummary::measurements)
            .map { it.cloudCoverageHistogram }
            .forEachIndexed { day, detailedHistogram ->
                if (detailedHistogram != null) {
                    detailedCloudCoverage[day] = detailedHistogram
                    detailedHistogram.forEachIndexed { coverage, count -> summarizedHistogram[coverage] += count }
                }
            }

    return SummarizedMeasurement(stationId = first().measurements.stationId,

                                 airTemperatureCentigrade = summarizeYearlyMinAvgMax { it.measurements.airTemperatureCentigrade },
                                 dewPointTemperatureCentigrade = summarizeYearlyMinAvgMax { it.measurements.dewPointTemperatureCentigrade },
                                 humidityPercent = summarizeYearlyMinAvgMax { it.measurements.humidityPercent },
                                 airPressureHectopascals = summarizeYearlyMinAvgMax { it.measurements.airPressureHectopascals },
                                 visibilityMeters = summarizeYearlyMinAvgMax { it.measurements.visibilityMeters },
                                 windSpeedMetersPerSecond = summarizeYearlyAvgMax { it.measurements.windSpeedMetersPerSecond },

                                 cloudCoverageHistogram = summarizedHistogram,
                                 detailedCloudCoverage = detailedCloudCoverage,

                                 sunshineMinutes = summarizeYearlySums { it.measurements.sunshineMinutes },
                                 rainfallMillimeters = summarizeYearlySums { it.measurements.rainfallMillimeters },
                                 snowfallMillimeters = summarizeYearlySums { it.measurements.snowfallMillimeters },

                                 detailedWindDirectionDegrees = Array(0) { null })
}

fun Collection<MonthlySummary>.summarizeMonthlyMinAvgMax(
        selector: (MonthlySummary) -> SummarizedMinAvgMax): SummarizedMinAvgMax {
    val minMeasurement = filter { selector(it).min != null }.minByOrNull { selector(it).min!! }
    val maxMeasurement = filter { selector(it).max != null }.maxByOrNull { selector(it).max!! }
    return SummarizedMinAvgMax(
            min = minMeasurement?.let(selector)?.min,
            minDate = LocalDate(minMeasurement?.year ?: 0, minMeasurement?.month ?: 1, 1),
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.let(selector)?.max,
            maxDate = LocalDate(maxMeasurement?.year ?: 0, maxMeasurement?.month ?: 1, 1))
}

fun Collection<MonthlySummary>.summarizeMonthlyAvgMax(
        selector: (MonthlySummary) -> SummarizedAvgMax): SummarizedAvgMax {
    val maxMeasurement = filter { selector(it).max != null }.maxByOrNull { selector(it).max!! }
    return SummarizedAvgMax(
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.let(selector)?.max,
            maxDate = LocalDate(maxMeasurement?.year ?: 0, maxMeasurement?.month ?: 1, 1))
}

fun Collection<MonthlySummary>.summarizeMonthlySums(
        selector: (MonthlySummary) -> SummarizedSum): SummarizedSum {
    val notNullSums = filter { selector(it).sum != null }
    val minMeasurement = notNullSums.maxByOrNull { selector(it).sum!! }
    val maxMeasurement = notNullSums.maxByOrNull { selector(it).sum!! }
    return SummarizedSum(
            min = minMeasurement?.let(selector)?.sum,
            minDate = LocalDate(minMeasurement?.year ?: 0, minMeasurement?.month ?: 1, 1),
            max = maxMeasurement?.let(selector)?.sum,
            maxDate = LocalDate(maxMeasurement?.year ?: 0, maxMeasurement?.month ?: 1, 1),
            sum = notNullSums.sumOf { selector(it).sum!! }
    )
}

fun Collection<YearlySummary>.summarizeYearlyMinAvgMax(
        selector: (YearlySummary) -> SummarizedMinAvgMax): SummarizedMinAvgMax {
    val minMeasurement = filter { selector(it).min != null }.minByOrNull { selector(it).min!! }
    val maxMeasurement = filter { selector(it).max != null }.maxByOrNull { selector(it).max!! }
    return SummarizedMinAvgMax(
            min = minMeasurement?.let(selector)?.min,
            minDate = LocalDate(minMeasurement?.year ?: 0, 1, 1),
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.let(selector)?.max,
            maxDate = LocalDate(maxMeasurement?.year ?: 0, 1, 1))
}

fun Collection<YearlySummary>.summarizeYearlyAvgMax(
        selector: (YearlySummary) -> SummarizedAvgMax): SummarizedAvgMax {
    val maxMeasurement = filter { selector(it).max != null }.maxByOrNull { selector(it).max!! }
    return SummarizedAvgMax(
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.let(selector)?.max,
            maxDate = LocalDate(maxMeasurement?.year ?: 0, 1, 1))
}

fun Collection<YearlySummary>.summarizeYearlySums(
        selector: (YearlySummary) -> SummarizedSum): SummarizedSum {
    val notNullSums = filter { selector(it).sum != null }
    val minMeasurement = notNullSums.maxByOrNull { selector(it).sum!! }
    val maxMeasurement = notNullSums.maxByOrNull { selector(it).sum!! }
    return SummarizedSum(
            min = minMeasurement?.let(selector)?.sum,
            minDate = LocalDate(minMeasurement?.year ?: 0, 1, 1),
            max = maxMeasurement?.let(selector)?.sum,
            maxDate = LocalDate(maxMeasurement?.year ?: 0, 1, 1),
            sum = notNullSums.sumOf { selector(it).sum!! }
    )
}
