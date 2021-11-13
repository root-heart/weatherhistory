package rootheart.codes.weatherhistory.importer

import java.util.concurrent.ConcurrentHashMap

data class StationId private constructor(val stationId: Int) {
    companion object {
        private val STATION_ID_MAP: Map<Int, StationId> = ConcurrentHashMap()

        @JvmStatic
        fun of(int: Int) = STATION_ID_MAP.getOrElse(int) { StationId(int) }

        @JvmStatic
        fun of(string: String) = of(Integer.parseInt(string))
    }
}