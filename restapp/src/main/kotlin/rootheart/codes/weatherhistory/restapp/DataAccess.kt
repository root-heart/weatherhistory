package rootheart.codes.weatherhistory.restapp

import rootheart.codes.weatherhistory.database.MeasurementColumns
import rootheart.codes.weatherhistory.database.MeasurementsTable

// still TODO "precipitation", "sunshine-duration", "wind"
val measurementTypeColumnsMapping: Map<String, MeasurementColumns<out Number?>> = mapOf(
    "temperature" to MeasurementColumns(
        MeasurementsTable.minAirTemperatureCentigrade,
        MeasurementsTable.avgAirTemperatureCentigrade,
        MeasurementsTable.maxAirTemperatureCentigrade,
        MeasurementsTable.detailedAirTemperatureCentigrade,
    ),
    "air-pressure" to MeasurementColumns(
        MeasurementsTable.minAirPressureHectopascals,
        MeasurementsTable.avgAirPressureHectopascals,
        MeasurementsTable.maxAirPressureHectopascals,
        MeasurementsTable.detailedAirPressureHectopascals,
    ),
    "dew-point-temperature" to MeasurementColumns(
        MeasurementsTable.minDewPointTemperatureCentigrade,
        MeasurementsTable.avgDewPointTemperatureCentigrade,
        MeasurementsTable.maxDewPointTemperatureCentigrade,
        MeasurementsTable.detailedAirPressureHectopascals
    ),
    "humidity" to MeasurementColumns(
        MeasurementsTable.minHumidityPercent,
        MeasurementsTable.avgHumidityPercent,
        MeasurementsTable.maxHumidityPercent,
        MeasurementsTable.detailedHumidityPercent
    ),
    "visibility" to MeasurementColumns(
        MeasurementsTable.minVisibilityMeters,
        MeasurementsTable.avgVisibilityMeters,
        MeasurementsTable.maxVisibilityMeters,
        MeasurementsTable.detailedVisibilityMeters
    )
)

