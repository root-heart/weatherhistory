package rootheart.codes.weatherhistory.importer

enum class PrecipitationType(val code: Int, val description: String) {
    NONE(0, "kein Niederschlag gefallenen und/ oder keine Niederschlagshöhe aus abgesetzten Niederschlägen (wie Tau, Reif), bei automatischen Stationen entspricht das der WMO-Code-Zahl 10"),
    DEW_OR_FROST(1, "Niederschlagshöhe ausschließlich aus abgesetzten Niederschlägen (fest und flüssig) oder es kann nicht zwischen fest und flüssig unterschieden werden"),
    DEW(2, "Niederschlagshöhe ausschließlich aus flüssigen abgesetzten Niederschlägen (\"weißer Tau\" wird den flüssigen abgesetzten Niederschlägen zugeordnet"),
    FROST(3, "Niederschlagshöhe ausschließlich aus festen abgesetzten Niederschlägen"),
    LIQUID(6, "gefallener Niederschlag nur in flüssiger Form, kann auch abgesetzten Niederschlag jeder Art enthalten, bei automatischen Stationen entspricht das der WMO-Code-Zahl 11"),
    SOLID(7, "gefallener Niederschlag nur in fester Form, kann auch abgesetzten Niederschlag jeder Art enthalten, bei automatischen Stationen entspricht das der WMO-Code-Zahl 12"),
    LIQUID_OR_SOLID(8, "gefallener Niederschlag in fester und flüssiger Form, kann auch abgesetzten Niederschlag jeder Art enthalten, bei automatischen Stationen entspricht das der WMO-CodeZahl 13"),
    MEASUREMENT_FAILED(9, "Niederschlagsmessung ausgefallen, die Niederschlagsform kann nicht festgestellt werden, bei automatischen Stationen entspricht das der WMO-Code-Zahl 15");

    companion object {
        private val PRECIPITATION_TYPE_BY_CODE: Map<Int, PrecipitationType>

        init {
            PRECIPITATION_TYPE_BY_CODE = HashMap ()
            for (precipitationType in values()) {
                PRECIPITATION_TYPE_BY_CODE.put(precipitationType.code, precipitationType)
            }
        }
    }
}
