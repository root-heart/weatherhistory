package rootheart.codes.weatherhistory.model

enum class MeasurementOrObservation(val code: String) {
    MEASUREMENT("I"),
    OBSERVATION("P");

    companion object {
        @JvmStatic
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