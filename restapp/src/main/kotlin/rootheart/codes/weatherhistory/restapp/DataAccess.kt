package rootheart.codes.weatherhistory.restapp

import rootheart.codes.weatherhistory.database.ExposedDao
import rootheart.codes.weatherhistory.database.JdbcDao
import rootheart.codes.weatherhistory.database.MeasurementsTable

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

object HourlyCoverageDao : JdbcDao(
    MeasurementsTable::hourlyCloudCoverage
)

object HourlySunshineDurationDao : JdbcDao(
    MeasurementsTable::hourlySunshineDurationMinutes,
    MeasurementsTable::sumSunshineDurationHours,
)

object DailySunshineDurationDao : JdbcDao(
    MeasurementsTable::sumSunshineDurationHours,
)