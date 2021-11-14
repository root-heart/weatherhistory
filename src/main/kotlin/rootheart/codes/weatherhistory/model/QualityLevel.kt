package rootheart.codes.weatherhistory.model

enum class QualityLevel(val code: Int, val description: String) {
    ONE(1, "nur formale Prüfung beim Entschlüsseln und Laden"),
    TWO(2, "nach individuellen Kriterien geprüft"),
    THREE(3, "in ROUTINE mit dem Verfahren QUALIMET und QCSY geprüft"),
    FIVE(5, "historische, subjektive Verfahren"),
    SEVEN(7, "in ROUTINE geprüft, aber keine Korrekturen"),
    EIGHT(8, "Qualitätssicherung außerhalb ROUTINE"),
    NINE(9, "in ROUTINE geprüft, nicht alle Parameter korrigiert"),
    TEN(10, "in ROUTINE geprüft, routinemäßige Korrektur beendet");

    companion object {
        private val QUALITY_LEVEL_BY_CODE: Map<Int, QualityLevel>

        init {
            QUALITY_LEVEL_BY_CODE = HashMap();
            for (qualityLevel in values()) {
                QUALITY_LEVEL_BY_CODE.put(qualityLevel.code, qualityLevel);
            }
        }

        @JvmStatic
        fun of(string: String) = of(Integer.parseInt(string))

        @JvmStatic
        fun of(int: Int) = QUALITY_LEVEL_BY_CODE.getValue(int)
    }
}