package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import org.joda.time.DateTime
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.database.TableMapping
import rootheart.codes.weatherhistory.database.WeatherDb
import java.math.BigDecimal
import java.sql.SQLException
import java.sql.Timestamp

open class RecordsImporter<POKO : Any>(private val tableMapping: TableMapping<POKO>) {
    private val tableName = determineTableName()
    private val insertSql = buildInsertSql()
    private val log = KotlinLogging.logger {}

    fun importEntities(entities: Collection<POKO>) {
        log.info { "importEntities($tableName, ${entities.size})" }
        try {
            WeatherDb.dataSource.connection.use { connection ->
                connection.prepareStatement(insertSql).use { statement ->
                    log.info { "Create batch" }
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
                    log.info { "Created batch" }
                    val affectedRows = statement.executeBatch()
                    log.info { "Batch executed, ${affectedRows.sum()} records affected" }
                }
            }
        } catch (e: SQLException) {
            log.error("importEntities($tableName, ${entities.size}) error during batch insert", e)
            throw e
        }
        log.info { "importEntities($tableName, ${entities.size}) finished" }
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

object SummarizedMeasurementImporter : RecordsImporter<SummarizedMeasurement>(SummarizedMeasurement.tableMapping)

object StationsImporter : RecordsImporter<Station>(Station.tableMapping)

object HourlyMeasurementsImporter : RecordsImporter<HourlyMeasurement>(HourlyMeasurement.tableMapping)