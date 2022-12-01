package rootheart.codes.weatherhistory.restapp

import rootheart.codes.weatherhistory.database.JdbcDao
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.MonthlySummaryTable
import rootheart.codes.weatherhistory.database.SummaryJdbcDao

object MonthlyTemperatureDao : SummaryJdbcDao(
    MonthlySummaryTable::minAirTemperatureCentigrade,
    MonthlySummaryTable::avgAirTemperatureCentigrade,
    MonthlySummaryTable::maxAirTemperatureCentigrade,
)

object MonthlyDewPointTemperatureDao : SummaryJdbcDao(
    MonthlySummaryTable::minDewPointTemperatureCentigrade,
    MonthlySummaryTable::avgDewPointTemperatureCentigrade,
    MonthlySummaryTable::maxDewPointTemperatureCentigrade,
)

object MonthlyHumidityDao : SummaryJdbcDao(
    MonthlySummaryTable::minHumidityPercent,
    MonthlySummaryTable::avgHumidityPercent,
    MonthlySummaryTable::maxHumidityPercent,
)

object MonthlySunshineDurationDao : SummaryJdbcDao(
    MonthlySummaryTable::sumSunshineDurationHours,
)

object MonthlyAirPressureDao : SummaryJdbcDao(
    MonthlySummaryTable::minAirPressureHectopascals,
    MonthlySummaryTable::avgAirPressureHectopascals,
    MonthlySummaryTable::maxAirPressureHectopascals,
)

object MonthlyWindDao : SummaryJdbcDao(
    MonthlySummaryTable::avgWindSpeedMetersPerSecond,
    MonthlySummaryTable::maxWindSpeedMetersPerSecond
)

object MonthlyPrecipitationDao : SummaryJdbcDao(
    MonthlySummaryTable::sumRainfallMillimeters,
    MonthlySummaryTable::sumSnowfallMillimeters
)

object DailyTemperatureDao : JdbcDao(
    MeasurementsTable::minAirTemperatureCentigrade,
    MeasurementsTable::avgAirTemperatureCentigrade,
    MeasurementsTable::maxAirTemperatureCentigrade
)

object HourlyTemperatureDao : JdbcDao(
    MeasurementsTable::hourlyAirTemperatureCentigrade,
    MeasurementsTable::minAirTemperatureCentigrade,
    MeasurementsTable::avgAirTemperatureCentigrade,
    MeasurementsTable::maxAirTemperatureCentigrade
)

object DailyDewPointTemperatureDao : JdbcDao(
    MeasurementsTable::minDewPointTemperatureCentigrade,
    MeasurementsTable::avgDewPointTemperatureCentigrade,
    MeasurementsTable::maxDewPointTemperatureCentigrade
)

object HourlyDewPointTemperatureDao : JdbcDao(
    MeasurementsTable::hourlyDewPointTemperatureCentigrade,
    MeasurementsTable::minDewPointTemperatureCentigrade,
    MeasurementsTable::avgDewPointTemperatureCentigrade,
    MeasurementsTable::maxDewPointTemperatureCentigrade
)

object DailyHumidityDao : JdbcDao(
    MeasurementsTable::minHumidityPercent,
    MeasurementsTable::avgHumidityPercent,
    MeasurementsTable::maxHumidityPercent
)

object HourlyHumidityDao : JdbcDao(
    MeasurementsTable::hourlyHumidityPercent,
    MeasurementsTable::minHumidityPercent,
    MeasurementsTable::avgHumidityPercent,
    MeasurementsTable::maxHumidityPercent
)

object DailyAirPressureDao : JdbcDao(
    MeasurementsTable::minAirPressureHectopascals,
    MeasurementsTable::avgAirPressureHectopascals,
    MeasurementsTable::maxAirPressureHectopascals,
)

object HourlyAirPressureDao : JdbcDao(
    MeasurementsTable::hourlyAirPressureHectopascals,
    MeasurementsTable::minAirPressureHectopascals,
    MeasurementsTable::avgAirPressureHectopascals,
    MeasurementsTable::maxAirPressureHectopascals,
)

object HourlyCoverageDao : JdbcDao(
    MeasurementsTable::hourlyCloudCoverage
)

object DailyPrecipitationDao : JdbcDao(
    MeasurementsTable::sumRainfallMillimeters,
    MeasurementsTable::sumSnowfallMillimeters,
)

object HourlyPrecipitationDao : JdbcDao(
    MeasurementsTable::hourlyRainfallMillimeters,
    MeasurementsTable::sumRainfallMillimeters,
    MeasurementsTable::hourlySnowfallMillimeters,
    MeasurementsTable::sumSnowfallMillimeters,
)

object HourlySunshineDurationDao : JdbcDao(
    MeasurementsTable::hourlySunshineDurationMinutes,
    MeasurementsTable::sumSunshineDurationHours,
)

object DailySunshineDurationDao : JdbcDao(
    MeasurementsTable::sumSunshineDurationHours,
)

object DailyWindDao : JdbcDao(
    MeasurementsTable::avgWindSpeedMetersPerSecond,
    MeasurementsTable::maxWindSpeedMetersPerSecond,
)

object HourlyWindDao : JdbcDao(
    MeasurementsTable::hourlyWindSpeedMetersPerSecond,
    MeasurementsTable::hourlyWindDirectionDegrees,
    MeasurementsTable::avgWindSpeedMetersPerSecond,
    MeasurementsTable::maxWindSpeedMetersPerSecond,
)

object MonthlyVisibilityDao : SummaryJdbcDao(
    MonthlySummaryTable::minVisibilityMeters,
    MonthlySummaryTable::avgVisibilityMeters,
    MonthlySummaryTable::maxVisibilityMeters,
)

object HourlyVisibilityDao : JdbcDao(
    MeasurementsTable::hourlyVisibilityMeters,
)
