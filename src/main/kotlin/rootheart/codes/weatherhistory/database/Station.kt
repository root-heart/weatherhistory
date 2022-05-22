package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

object StationsTable : LongIdTable("STATIONS") {
    val externalId = varchar("EXTERNAL_ID", 10).uniqueIndex()
    val name = varchar("NAME", 50)
    val federalState = varchar("FEDERAL_STATE", 50)
    val height = integer("HEIGHT")
    val latitude = decimal("LATITUDE", 7, 4)
    val longitude = decimal("LONGITUDE", 7, 4)
}

object StationTableMapping : TableMapping<Station>(
    Station::externalId to StationsTable.externalId,
    Station::name to StationsTable.name,
    Station::federalState to StationsTable.federalState,
    Station::height to StationsTable.height,
    Station::latitude to StationsTable.latitude,
    Station::longitude to StationsTable.longitude
)

data class Station(
    val id: Long? = null,
    val externalId: String,
    val name: String,
    val federalState: String,
    val height: Int,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)

object StationDao {
    fun findAll() = transaction {
        StationsTable.selectAll().map(StationDao::fromResultRow)
    }

    fun findById(id: Long) = transaction {
        StationsTable.select { StationsTable.id eq id }
            .map(::fromResultRow)
            .firstOrNull()
    }

    fun findStationByExternalId(stationId: String): Station? = transaction {
        StationsTable.select { StationsTable.externalId eq stationId }
            .map(::fromResultRow)
            .firstOrNull()
    }

    fun findAllMappedById() = transaction {
        StationsTable.selectAll().associateBy({ it[StationsTable.id].value }, { fromResultRow(it) })
    }

    private fun fromResultRow(row: ResultRow) = Station(
        id = row[StationsTable.id].value,
        externalId = row[StationsTable.externalId],
        name = row[StationsTable.name],
        federalState = row[StationsTable.federalState],
        height = row[StationsTable.height],
        latitude = row[StationsTable.latitude],
        longitude = row[StationsTable.longitude]
    )
}