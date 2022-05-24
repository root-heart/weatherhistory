package rootheart.codes.weatherhistory.database

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
//import org.apache.tools.ant.filters.StringInputStream
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import java.sql.SQLException
import kotlin.system.measureTimeMillis

private const val BATCH_SIZE = 128 * 1024

@DelicateCoroutinesApi
private val insertThreadPool = newFixedThreadPoolContext(32, "database-inserter")

@DelicateCoroutinesApi
open class DatabaseInserter<POKO : Any>(private val tableMapping: TableMapping<POKO>) {
    private val tableName = determineTableName()
    private val copyFromSql = buildCopyFromSql()
    private val log = KotlinLogging.logger {}

    fun importEntities(entities: Collection<POKO>) {

        log.info { "importEntities($tableName, ${entities.size} entities)" }
        try {
            val chunks = entities.chunked(BATCH_SIZE)
            runBlocking {
                chunks.forEach { chunk ->
                    launch(insertThreadPool) {
                        copyIntoTable(chunk)
                    }
                }
            }
        } catch (e: SQLException) {
            log.error(e) { "importEntities($tableName, ${entities.size} entities) error during batch insert" }
            throw e
        }
        log.info { "importEntities($tableName, ${entities.size} entities) finished" }
    }

    private fun copyIntoTable(entities: Collection<POKO>) {
        WeatherDb.dataSource.connection.use { connection ->
            val pgConnection = connection.unwrap(BaseConnection::class.java)
            val copyManager = CopyManager(pgConnection)

            var csv: String
            val timeCreatingStrings = measureTimeMillis {
                csv = entities.joinToString("\n") { entity ->
                    tableMapping.keys.map { it.get(entity) ?: "\\N" }.joinToString("|")
                }
            }
            log.info { "Creating the CSV for ${entities.size} rows took $timeCreatingStrings millis" }
            val timeCopying = measureTimeMillis { copyManager.copyIn(copyFromSql, csv.byteInputStream()) }
            log.info { "Copying ${entities.size} records took $timeCopying millis" }
        }
    }

    private fun determineTableName(): String {
        val distinctTables = tableMapping.values.map { it.table }.distinct()
        if (distinctTables.size != 1) {
            throw Exception()
        }
        return distinctTables[0].tableName
    }

    private fun buildCopyFromSql(): String {
        val databaseColumnNames = tableMapping.values.joinToString(", ") { it.name }
        return "COPY $tableName ($databaseColumnNames) FROM STDIN WITH DELIMITER '|'"
    }
}

@DelicateCoroutinesApi
object SummarizedMeasurementImporter : DatabaseInserter<SummarizedMeasurement>(SummarizedMeasurementTableMapping)

@DelicateCoroutinesApi
object StationsImporter : DatabaseInserter<Station>(StationTableMapping)

@DelicateCoroutinesApi
object HourlyMeasurementsImporter : DatabaseInserter<HourlyMeasurement>(HourlyMeasurementTableMapping)