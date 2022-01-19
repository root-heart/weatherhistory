package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.TableMapping
import rootheart.codes.weatherhistory.database.WeatherDb
import java.math.BigDecimal
import java.sql.Date
import java.sql.SQLException

class RecordsImporter<POKO : Any>(private val tableMapping: TableMapping<POKO>) {
    private val tableName = determineTableName()
    private val insertSql = buildInsertSql()
    private val log = KotlinLogging.logger {}

    fun importEntities(entities: Collection<POKO>) {
        log.info { "Import data into table $tableName" }
        try {
            WeatherDb.dataSource.connection.use { connection ->
                connection.prepareStatement(insertSql).use { statement ->
                    log.info { "Create batch" }
                    for (entity in entities) {
                        var parameterIndex = 1
                        for (property in tableMapping.keys) {
                            when (val value = property.get(entity)) {
                                is BigDecimal -> statement.setBigDecimal(parameterIndex, value)
                                is LocalDate -> statement.setDate(
                                    parameterIndex,
                                    Date(value.toDateTimeAtStartOfDay().millis)
                                )
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
            log.error("Error during batch insert", e)
        }
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