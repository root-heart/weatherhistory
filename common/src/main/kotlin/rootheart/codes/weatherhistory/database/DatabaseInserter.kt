package rootheart.codes.weatherhistory.database

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
//import org.apache.tools.ant.filters.StringInputStream
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import java.io.ByteArrayInputStream
import java.sql.SQLException
import kotlin.system.measureTimeMillis

private const val BATCH_SIZE = 1 * 1024

@DelicateCoroutinesApi
private val insertThreadPool = newFixedThreadPoolContext(32, "database-inserter")

@DelicateCoroutinesApi
open class DatabaseInserter<POKO : Any>(private val tableMapping: TableMapping<POKO>) {
    private val tableName = determineTableName()
    private val copyFromSql = buildCopyFromSql()
    private val log = KotlinLogging.logger {}

    fun importEntities(entities: Collection<POKO>) {
        log.debug { "importEntities($tableName, ${entities.size} entities)" }
        val duration = measureTimeMillis {
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
        }
        log.info { "importEntities($tableName, ${entities.size} entities) finished in $duration millis" }
    }

    private fun copyIntoTable(entities: Collection<POKO>) {
        WeatherDb.dataSource.connection.use { connection ->
            val pgConnection = connection.unwrap(BaseConnection::class.java)
            val copyManager = CopyManager(pgConnection)

            var csv: ByteArrayInputStream
            val timeCreatingStrings = measureTimeMillis {
                val s = entities.joinToString("\n") { entity ->
                    tableMapping.keys
                        .map { property ->
                            when (val value = property.get(entity)) {
                                null -> return@map "\\N"
                                is Array<*> -> return@map value.joinToString(",")
                                else -> return@map value
                            }
                        }
                        .joinToString("|")
                }
                csv = s.byteInputStream()
            }
            log.debug { "Creating the CSV for ${entities.size} rows took $timeCreatingStrings millis" }
            val timeCopying = measureTimeMillis { copyManager.copyIn(copyFromSql, csv) }
            log.debug { "Copying ${entities.size} records took $timeCopying millis" }
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
object MeasurementImporter : DatabaseInserter<Measurement>(MeasurementTableMapping)

@DelicateCoroutinesApi
object MonthlySummaryImporter : DatabaseInserter<MonthlySummary>(MonthlySummaryTableMapping)

@DelicateCoroutinesApi
object StationsImporter : DatabaseInserter<Station>(StationTableMapping)
