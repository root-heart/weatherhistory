package rootheart.codes.weatherhistory.database.daily

import java.math.BigDecimal

class DailyMeasurementEntity(
        val stationId: Long,
        val dateInUtcMillis: Long,

        val airTemperatureCentigrade: DailyMinAvgMax = DailyMinAvgMax(),
        val dewPointTemperatureCentigrade: DailyMinAvgMax = DailyMinAvgMax(),
        val humidityPercent: DailyMinAvgMax = DailyMinAvgMax(),
        val airPressureHectopascals: DailyMinAvgMax = DailyMinAvgMax(),
        val visibilityMeters: DailyMinAvgMax = DailyMinAvgMax(),
        val sunshineMinutes: DailySum<Int> = DailySum(),
        val rainfallMillimeters: DailySum<BigDecimal> = DailySum(),
        val snowfallMillimeters: DailySum<BigDecimal> = DailySum(),
        val windSpeedMetersPerSecond: DailyAvgMax = DailyAvgMax(),
        val windDirectionDegrees: DailyMinMax = DailyMinMax(),
        var cloudCoverage: DailyDetailsAndHistogram = DailyDetailsAndHistogram()
)

class DailyMinMax(
        var min: BigDecimal? = null,
        var max: BigDecimal? = null,
        var details: Array<BigDecimal?>? = null,
)

class DailyAvgMax(
        var avg: BigDecimal? = null,
        var max: BigDecimal? = null,
        var details: Array<BigDecimal?>? = null,
)

class DailyMinAvgMax(
        var min: BigDecimal? = null,
        var avg: BigDecimal? = null,
        var max: BigDecimal? = null,
        var details: Array<BigDecimal?>? = null,
)

class DailySum<T : Comparable<T>>(
        var sum: T? = null,
        var details: Array<T?>? = null,
)

class DailyDetailsAndHistogram(
        var details: Array<Int?>? = null,
        var histogram: Array<Int>? = null
)