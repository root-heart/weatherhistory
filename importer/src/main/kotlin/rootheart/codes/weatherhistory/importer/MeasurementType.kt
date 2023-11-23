package rootheart.codes.weatherhistory.importer

enum class MeasurementType(
    val abbreviation: String,
//    val propertyByName: Map<String, MeasurementProperty<*>>
) {
    AIR_TEMPERATURE("TU"),

//    CLOUD_TYPE("CS", mapOf("V_N" to IntProperty(HourlyMeasurement::cloudCoverage))),

    CLOUD_COVERAGE("N"),

    DEW_POINT("TD"),

    MAX_WIND_SPEED("FX"),

    MOISTURE("TF"),

//    SOIL_TEMPERATURE("EB", mapOf()),

    SUNSHINE_DURATION("SD"),

    VISIBILITY("VV"),

    WIND_SPEED("FF"),

    PRECIPITATION("RR"),

    DAILY("KL")
    ;

    companion object {
        fun of(abbreviation: String) = values().first { it.abbreviation == abbreviation }
    }
}
