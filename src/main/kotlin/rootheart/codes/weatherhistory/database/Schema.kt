package rootheart.codes.weatherhistory.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.sql.DataSource
import kotlin.reflect.KProperty1

fun main() {
    WeatherDb.dropTables()
    WeatherDb.createTables()
}

abstract class TableMapping<POKO>(vararg val mappings: Pair<KProperty1<POKO, Any?>, Column<out Any?>>) {
    val values = mappings.map { it.second }
    val keys = mappings.map { it.first }
}

object WeatherDb {
    val dataSource: DataSource = createDataSource()

    private fun createDataSource(): DataSource {
        val properties = Properties()
        properties.load(javaClass.getResourceAsStream("/application.properties"))

        val config = HikariConfig()
        config.jdbcUrl = properties.getProperty("jdbc.url")
        config.username = properties.getProperty("jdbc.username")
        config.password = properties.getProperty("jdbc.password")
        config.driverClassName = properties.getProperty("jdbc.driverClassName")
        config.dataSourceProperties = Properties()
        config.dataSourceProperties["reWriteBatchedInserts"] = "true"
        config.minimumIdle = properties.getProperty("jdbc.minimumIdlePoolSize", "10").toInt()
        config.maximumPoolSize = properties.getProperty("jdbc.maximumPoolSize", "30").toInt()
//        config.dataSourceProperties["loggerLevel"] = "TRACE"
        return HikariDataSource(config)
    }

    fun createTables() {
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(HourlyMeasurementsTable, SummarizedMeasurementsTable, StationsTable)
        }
    }

    fun dropTables() {
        Database.connect(dataSource)
        transaction {
            SchemaUtils.drop(HourlyMeasurementsTable, SummarizedMeasurementsTable, StationsTable)
        }
    }
}

