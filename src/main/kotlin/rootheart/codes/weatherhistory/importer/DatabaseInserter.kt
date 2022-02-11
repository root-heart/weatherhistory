package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.DateTime
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.HourlyMeasurementTableMapping
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.StationTableMapping
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.SummarizedMeasurementTableMapping
import rootheart.codes.weatherhistory.database.TableMapping
import rootheart.codes.weatherhistory.database.WeatherDb
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Timestamp

private const val BATCH_SIZE = 100_000

@DelicateCoroutinesApi
private val insertThreadPool = newFixedThreadPoolContext(10, "database-inserter")

@DelicateCoroutinesApi
open class DatabaseInserter<POKO : Any>(private val tableMapping: TableMapping<POKO>) {
    private val tableName = determineTableName()
    private val insertSql = buildInsertSql()
    private val log = KotlinLogging.logger {}

    fun importEntities(entities: Collection<POKO>) {
        log.info { "importEntities($tableName, ${entities.size} entities)" }
        try {
            if (entities.size < BATCH_SIZE) {
                executeInsert(entities)
            } else {
                insertChunked(entities)
            }
        } catch (e: SQLException) {
            log.error("importEntities($tableName, ${entities.size} entities) error during batch insert", e)
            throw e
        }
        log.info { "importEntities($tableName, ${entities.size} entities) finished" }
    }

    private fun insertChunked(entities: Collection<POKO>) {
        val chunks = entities.chunked(BATCH_SIZE)
        runBlocking {
            chunks.forEach { chunk ->
                launch(insertThreadPool) {
                    executeInsert(chunk)
                }
            }
        }
    }

    private fun executeInsert(entities: Collection<POKO>) {
        WeatherDb.dataSource.connection.use { connection ->
            connection.prepareStatement(insertSql).use { statement ->
                createInsertBatch(entities, statement)
                val affectedRows = statement.executeBatch()
                log.info { "Batch executed, ${affectedRows.sum()} records affected" }
            }
        }
    }

    private fun createInsertBatch(entities: Collection<POKO>, statement: PreparedStatement) {
        log.info { "createInsertBatch(${entities.size})" }
        for (entity in entities) {
            var parameterIndex = 1
            for (property in tableMapping.keys) {
                when (val value = property.get(entity)) {
                    is BigDecimal -> statement.setBigDecimal(parameterIndex, value)
                    is DateTime -> statement.setTimestamp(parameterIndex, Timestamp(value.millis))
                    is String -> statement.setString(parameterIndex, value)
                    else -> statement.setObject(parameterIndex, value)
                }
                parameterIndex++
            }
            statement.addBatch()
        }
        log.info { "createInsertBatch(${entities.size}) finished" }
    }

    private fun determineTableName(): String {
        val distinctTables = tableMapping.values.map { it.table }.distinct()
        if (distinctTables.size != 1) {
            throw Exception()
        }
        return distinctTables[0].tableName
    }

    private fun buildInsertSql(): String {
        val databaseColumnNames = tableMapping.values.joinToString(", ") { it.name }
        val parameterQuestionMarks = tableMapping.values.joinToString(", ") { "?" }
        return ("INSERT INTO " + tableName + " (" + databaseColumnNames + " ) "
                + "VALUES (" + parameterQuestionMarks + ") "
                + "ON CONFLICT DO NOTHING")
    }
}

object SummarizedMeasurementImporter : DatabaseInserter<SummarizedMeasurement>(SummarizedMeasurementTableMapping)

object StationsImporter : DatabaseInserter<Station>(StationTableMapping)

object HourlyMeasurementsImporter : DatabaseInserter<HourlyMeasurement>(HourlyMeasurementTableMapping)