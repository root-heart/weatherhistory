package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import rootheart.codes.common.collections.AvgMax
import rootheart.codes.common.collections.MinAvgMax
import rootheart.codes.common.collections.MinMaxSumDetails
import rootheart.codes.weatherhistory.database.*

private val log = KotlinLogging.logger {}

fun insertMeasurementsIntoDatabase(measurements: List<MeasurementEntity>) = transaction {
    val stationIds = measurements.mapNotNull { it.stationId }.distinct()
    val query = StationsTable.select { StationsTable.id.inList(stationIds) }
    val stationIdById = transaction { query.map { row -> row[StationsTable.id] }.associateBy { it.value } }
    MeasurementsTable.batchInsert(measurements) {
        this[MeasurementsTable.stationId] = stationIdById[it.stationId]!!
        this[MeasurementsTable.year] = it.firstDayDateTime.year
        this[MeasurementsTable.month] = it.firstDayDateTime.monthOfYear
        this[MeasurementsTable.day] = it.firstDayDateTime.dayOfMonth
        this[MeasurementsTable.interval] = it.interval

        copyMinAvgMax(it.temperature, MeasurementsTable.temperatures)
        copyMinAvgMax(it.dewPointTemperature, MeasurementsTable.dewPointTemperatures)
        copyMinAvgMax(it.humidity, MeasurementsTable.humidity)
        copyMinAvgMax(it.airPressure, MeasurementsTable.airPressure)
        copyMinAvgMax(it.visibility, MeasurementsTable.visibility)
        copyAvgMax(it.windSpeed, MeasurementsTable.windSpeed)

        this[MeasurementsTable.cloudCoverage.histogram] = it.cloudCoverage.histogram ?: Array(0) { 0 }
        this[MeasurementsTable.cloudCoverage.details] = it.cloudCoverage.details

        copySum(it.sunshine, MeasurementsTable.sunshine)
        copySum(it.rainfall, MeasurementsTable.rainfall)
        copySum(it.snowfall, MeasurementsTable.snowfall)

        this[MeasurementsTable.detailedWindDirectionDegrees] = it.detailedWindDirectionDegrees
    }
    log.info { "Inserted ${measurements.size} objects into the database" }
}

private fun <N : Number> BatchInsertStatement.copyMinAvgMax(from: MinAvgMax<N>, to: MinAvgMaxDetailsColumns<N>) {
    this[to.min] = from.min
    this[to.minDay] = from.minDay?.toDateTimeAtStartOfDay()
    this[to.avg] = from.avg
    this[to.max] = from.max
    this[to.maxDay] = from.maxDay?.toDateTimeAtStartOfDay()
    this[to.details] = from.details
}

private fun <N : Number> BatchInsertStatement.copyAvgMax(from: AvgMax<N>, to: AvgMaxDetailsColumns<N>) {
    this[to.avg] = from.avg
    this[to.max] = from.max
    this[to.maxDay] = from.maxDay?.toDateTimeAtStartOfDay()
    this[to.details] = from.details
}

private fun <N : Number> BatchInsertStatement.copySum(from: MinMaxSumDetails<N>, to: MinMaxSumDetailsColumns<N>) {
    this[to.min] = from.min
    this[to.minDay] = from.minDay?.toDateTimeAtStartOfDay()
    this[to.max] = from.max
    this[to.maxDay] = from.maxDay?.toDateTimeAtStartOfDay()
    this[to.sum] = from.sum
    this[to.details] = from.details
}

