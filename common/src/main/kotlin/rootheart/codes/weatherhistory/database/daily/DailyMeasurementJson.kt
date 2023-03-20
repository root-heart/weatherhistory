package rootheart.codes.weatherhistory.database.daily

import org.joda.time.LocalDate
import java.math.BigDecimal

// TODO clarify if separate JSON class is needed or if Entity class can be used
class DailyMeasurementJson(
        val date: LocalDate,
        val airTemperatureCentigrade: DailyMinAvgMax = DailyMinAvgMax(),
        val dewPointTemperatureCentigrade: DailyMinAvgMax = DailyMinAvgMax(),
        val humidityPercent: DailyMinAvgMax = DailyMinAvgMax(),
        val airPressureHectopascals: DailyMinAvgMax = DailyMinAvgMax(),
        var cloudCoverage: List<Int> = emptyList(),
        val sunshineMinutes: DailySum = DailySum(),
        val rainfallMillimeters: DailySum = DailySum(),
        val snowfallMillimeters: DailySum = DailySum(),
        val windSpeedMetersPerSecond: DailyAvgMax = DailyAvgMax(),
        val visibilityMeters: DailyMinAvgMax = DailyMinAvgMax(),
)