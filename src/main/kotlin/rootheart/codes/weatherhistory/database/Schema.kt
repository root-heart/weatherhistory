package rootheart.codes.weatherhistory.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.sql.DataSource

fun main() {
    WeatherDb.createTables()
}

object WeatherDb {
    val dataSource: DataSource = createDataSource()

    private fun createDataSource(): DataSource {
        val properties = Properties()
        properties.load(javaClass.getResourceAsStream("/application.properties"))

        val config = HikariConfig()
        config.jdbcUrl = properties.getProperty("config.jdbcUrl")
        config.username = properties.getProperty("config.username")
        config.password = properties.getProperty("config.password")
        config.driverClassName = properties.getProperty("config.driverClassName")
        config.dataSourceProperties = Properties()
//        config.dataSourceProperties["reWriteBatchedInserts"] = "true"
//        config.dataSourceProperties["loggerLevel"] = "TRACE"
        return HikariDataSource(config)
    }

    fun createTables() {
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(SummarizedMeasurementsTable)
        }
    }

    fun dropTables() {
        Database.connect(dataSource)
        transaction {
            SchemaUtils.drop(SummarizedMeasurementsTable)
        }
    }
}

