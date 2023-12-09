package rootheart.codes.weatherhistory.database.daily

import org.joda.time.LocalDate
import java.math.BigDecimal


class DailyMeasurementEntity(
        val stationId: Long,
        val dateInUtcMillis: Long,

        val airTemperatureCentigrade: DailyMinAvgMax = DailyMinAvgMax(),
        val dewPointTemperatureCentigrade: DailyMinAvgMax = DailyMinAvgMax(),
        val humidityPercent: DailyMinAvgMax = DailyMinAvgMax(),
        val airPressureHectopascals: DailyMinAvgMax = DailyMinAvgMax(),
        val visibilityMeters: DailyMinAvgMax = DailyMinAvgMax(),
        val sunshineMinutes: DailySum = DailySum(),
        val rainfallMillimeters: DailySum = DailySum(),
        val snowfallMillimeters: DailySum = DailySum(),
        val windSpeedMetersPerSecond: DailyAvgMax = DailyAvgMax(),
        val windDirectionDegrees: DailyMinMax = DailyMinMax(),

        var detailedCloudCoverage: Array<Int?>? = null,
        var cloudCoverageHistogram: Array<Int>? = null
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

class DailySum(
        var sum: BigDecimal? = null,
        var details: Array<BigDecimal?>? = null,
)

