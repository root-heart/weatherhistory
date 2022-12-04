package rootheart.codes.weatherhistory.restapp

import rootheart.codes.weatherhistory.database.DAO
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.MinAvgMaxDao
import rootheart.codes.weatherhistory.database.SumDao

// still TODO "precipitation", "sunshine-duration", "wind"
val measurementTypeColumnsMapping: Map<String, DAO<*, out Number?>> = mapOf(
    "temperature" to MinAvgMaxDao(
        MeasurementsTable.minAirTemperatureCentigrade,
        MeasurementsTable.avgAirTemperatureCentigrade,
        MeasurementsTable.maxAirTemperatureCentigrade,
        MeasurementsTable.detailedAirTemperatureCentigrade,
    ),
    "air-pressure" to MinAvgMaxDao(
        MeasurementsTable.minAirPressureHectopascals,
        MeasurementsTable.avgAirPressureHectopascals,
        MeasurementsTable.maxAirPressureHectopascals,
        MeasurementsTable.detailedAirPressureHectopascals,
    ),
    "dew-point-temperature" to MinAvgMaxDao(
        MeasurementsTable.minDewPointTemperatureCentigrade,
        MeasurementsTable.avgDewPointTemperatureCentigrade,
        MeasurementsTable.maxDewPointTemperatureCentigrade,
        MeasurementsTable.detailedAirPressureHectopascals
    ),
    "humidity" to MinAvgMaxDao(
        MeasurementsTable.minHumidityPercent,
        MeasurementsTable.avgHumidityPercent,
        MeasurementsTable.maxHumidityPercent,
        MeasurementsTable.detailedHumidityPercent
    ),
    "visibility" to MinAvgMaxDao(
        MeasurementsTable.minVisibilityMeters,
        MeasurementsTable.avgVisibilityMeters,
        MeasurementsTable.maxVisibilityMeters,
        MeasurementsTable.detailedVisibilityMeters
    ),
    "wind-speed" to MinAvgMaxDao(
        null,
        MeasurementsTable.avgWindSpeedMetersPerSecond,
        MeasurementsTable.maxWindSpeedMetersPerSecond,
        MeasurementsTable.detailedWindSpeedMetersPerSecond
    ),
    "sunshine-duration" to SumDao(
        MeasurementsTable.sumSunshineDurationHours
    ),
    "precipitation" to SumDao(
        MeasurementsTable.sumRainfallMillimeters,
        MeasurementsTable.sumSnowfallMillimeters
    ),
)

