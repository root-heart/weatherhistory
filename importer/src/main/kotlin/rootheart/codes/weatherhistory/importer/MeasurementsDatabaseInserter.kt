package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import rootheart.codes.weatherhistory.database.AvgMaxColumns
import rootheart.codes.weatherhistory.database.Decimals
import rootheart.codes.weatherhistory.database.DecimalsColumns
import rootheart.codes.weatherhistory.database.Integers
import rootheart.codes.weatherhistory.database.IntegersColumns
import rootheart.codes.weatherhistory.database.Measurement
import rootheart.codes.weatherhistory.database.MeasurementsTable
import rootheart.codes.weatherhistory.database.MinAvgMax
import rootheart.codes.weatherhistory.database.MinAvgMaxColumns
import rootheart.codes.weatherhistory.database.StationsTable

private val log = KotlinLogging.logger {}

fun insertMeasurementsIntoDatabase(measurements: List<Measurement>) = transaction {
    val stationIds = measurements.mapNotNull { it.stationId }.distinct()
    val query = StationsTable.select { StationsTable.id.inList(stationIds) }
    val stationIdById = transaction { query.map { row -> row[StationsTable.id] }.associateBy { it.value } }
    MeasurementsTable.batchInsert(measurements) {
        this[MeasurementsTable.stationId] = stationIdById[it.stationId]!!
        this[MeasurementsTable.year] = it.firstDayDateTime.year
        this[MeasurementsTable.month] = it.firstDayDateTime.monthOfYear
        this[MeasurementsTable.day] = it.firstDayDateTime.dayOfMonth
        this[MeasurementsTable.interval] = it.interval

        copyMinAvgMax(it.temperatures, MeasurementsTable.temperatures)
        copyMinAvgMax(it.dewPointTemperatures, MeasurementsTable.dewPointTemperatures)
        copyMinAvgMax(it.humidity, MeasurementsTable.humidity)
        copyMinAvgMax(it.airPressure, MeasurementsTable.airPressure)
        copyMinAvgMax(it.visibility, MeasurementsTable.visibility)
        copyAvgMax(it.wind, MeasurementsTable.windSpeed)

        this[MeasurementsTable.cloudCoverage.histogram] = it.cloudCoverage.histogram
        this[MeasurementsTable.cloudCoverage.details] = it.cloudCoverage.details

        copySum(it.sunshineDuration, MeasurementsTable.sunshineDuration)
        copySum(it.rainfall, MeasurementsTable.rainfall)
        copySum(it.snowfall, MeasurementsTable.snowfall)

        this[MeasurementsTable.detailedWindDirectionDegrees] = it.detailedWindDirectionDegrees
    }
    log.info { "Inserted ${measurements.size} objects into the database" }
}

private fun <N : Number> BatchInsertStatement.copyMinAvgMax(from: MinAvgMax<N?>, to: MinAvgMaxColumns<N>) {
    this[to.min] = from.min
    this[to.avg] = from.avg
    this[to.max] = from.max
    this[to.details] = from.details
}

private fun <N : Number> BatchInsertStatement.copyAvgMax(from: MinAvgMax<N?>, to: AvgMaxColumns<N>) {
    this[to.avg] = from.avg
    this[to.max] = from.max
    this[to.details] = from.details
}

private fun BatchInsertStatement.copySum(from: Integers, to: IntegersColumns) {
    this[to.sum] = from.sum
    this[to.details] = from.values
}

private fun BatchInsertStatement.copySum(from: Decimals, to: DecimalsColumns) {
    this[to.sum] = from.sum
    this[to.details] = from.values
}
