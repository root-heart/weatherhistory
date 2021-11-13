package rootheart.codes.weatherhistory.importer

enum class CloudType(val description: String, val code: Int, val abbreviation: String) {
    CIRRUS("Cirrus", 0, "CI"),
    CIRROCUMULUS("Cirrocumulus", 1, "CC"),
    CIRROSTRATUS("Cirrostratus", 2, "CS"),
    ALTOCUMULUS("Altocumulus", 3, "AC"),
    ALTOSTRATUS("Altostratus", 4, "AS"),
    NIMBOSTRATUS("Nimbostratus", 5, "NS"),
    STRATOCUMULUS("Stratocumulus", 6, "SC"),
    STRATUS("Stratus", 7, "ST"),
    CUMULUS("Cumulus", 8, "CU"),
    CUMULONIMBUS("Cumulonimbus", 9, "CB"),
    GAUGE_MEASUREMENT("Instrumentenmessung", -1, "-1");

    companion object {
        private val CLOUD_TYPE_BY_CODE: MutableMap<Int, CloudType> = HashMap()

        init {
            for (cloudType in values()) {
                CLOUD_TYPE_BY_CODE[cloudType.code] = cloudType
            }
        }

        fun of(code: Int) = CLOUD_TYPE_BY_CODE.getValue(code)
    }
}
