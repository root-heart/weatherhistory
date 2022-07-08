package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.database.HourlyMeasurement

enum class MeasurementType(
    val abbreviation: String,
    val propertyByName: Map<String, MeasurementProperty<*>>
) {
    AIR_TEMPERATURE(
        "TU", mapOf(
            "TT_TU" to BigDecimalProperty(HourlyMeasurement::airTemperatureAtTwoMetersHeightCentigrade),
            "RF_TU" to BigDecimalProperty(HourlyMeasurement::relativeHumidityPercent)
        )
    ),

//    CLOUD_TYPE("CS", mapOf("V_N" to IntProperty(HourlyMeasurement::cloudCoverage))),

    CLOUDINESS("N", mapOf("V_N" to IntProperty(HourlyMeasurement::cloudCoverage))),

    DEW_POINT(
        "TD", mapOf(
            "TD" to BigDecimalProperty(HourlyMeasurement::dewPointTemperatureCentigrade),
        )
    ),

    MAX_WIND_SPEED("FX", mapOf("FX_911" to BigDecimalProperty(HourlyMeasurement::maxWindSpeedMetersPerSecond))),

    MOISTURE("TF", mapOf("P_STD" to BigDecimalProperty(HourlyMeasurement::airPressureHectopascals))),

//    SOIL_TEMPERATURE("EB", mapOf()),

    SUNSHINE_DURATION("SD", mapOf("SD_SO" to BigDecimalProperty(HourlyMeasurement::sunshineDurationMinutes))),

    VISIBILITY("VV", mapOf("V_VV" to IntProperty(HourlyMeasurement::visibilityInMeters))),

    WIND_SPEED(
        "FF", mapOf(
            "F" to BigDecimalProperty(HourlyMeasurement::windSpeedMetersPerSecond),
            "D" to IntProperty(HourlyMeasurement::windDirectionDegrees)
        )
    ),

    PRECIPITATION(
        "RR", mapOf(
            "R1" to BigDecimalProperty(HourlyMeasurement::precipitationMillimeters),
            "WRTR" to PrecipitationTypeProperty(HourlyMeasurement::precipitationType)
        )
    );

    companion object {
        fun of(abbreviation: String) = values().first { it.abbreviation == abbreviation }
    }
}

enum class MeasurementType2(val abbreviation: String) {
    AIR_TEMPERATURE("TU"),
    CLOUD_TYPE("CS"),
    CLOUDINESS("N"),
    DEW_POINT("TD"),
    MAX_WIND_SPEED("FX"),
    MOISTURE("TF"),
    SUNSHINE_DURATION("SD"),
    VISIBILITY("VV"),
    WIND_SPEED("FF"),
    PRECIPITATION("RR");

    companion object {
        fun of(abbreviation: String) = values().first { it.abbreviation == abbreviation }
    }
}