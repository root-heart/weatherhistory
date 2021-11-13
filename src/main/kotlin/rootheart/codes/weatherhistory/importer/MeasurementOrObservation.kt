package rootheart.codes.weatherhistory.importer

enum class MeasurementOrObservation(val code: String) {
    MEASUREMENT("I"),
    OBSERVATION("P");

    companion object {
        fun of(string: String): MeasurementOrObservation {
            for (measurementOrObservation in values()) {
                if (string == measurementOrObservation.code) {
                    return measurementOrObservation
                }
            }
            throw IllegalArgumentException()
        }
    }
}