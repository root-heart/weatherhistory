package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal

object StationsTable : LongIdTable("STATIONS") {
    val stationId = integer("STATION_ID").uniqueIndex()
    val name = varchar("NAME", 50)
    val federalState = varchar("FEDERAL_STATE", 50)
    val height = integer("HEIGHT")
    val latitude = decimal("LATITUDE", 7, 4)
    val longitude = decimal("LONGITUDE", 7, 4)
}


class Station(
    val stationId: StationId,
    val name: String,
    val federalState: String,
    val height: Int,
    val latitude: BigDecimal,
    val longitude: BigDecimal
) {
    val stationIdInt get() = stationId.stationId

    companion object {
        val tableMapping: TableMapping<Station> = mapOf(
            Station::stationIdInt to StationsTable.stationId,
            Station::name to StationsTable.name,
            Station::federalState to StationsTable.federalState,
            Station::height to StationsTable.height,
            Station::latitude to StationsTable.latitude,
            Station::longitude to StationsTable.longitude
        )
    }
}