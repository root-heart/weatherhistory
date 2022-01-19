package rootheart.codes.weatherhistory.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.sql.DataSource

fun main() {
    Database.connect(WeatherDb.dataSource)
    transaction {
        SchemaUtils.create(SummarizedMeasurementsTable)
    }
}

object WeatherDb {
    val dataSource: DataSource = createDataSource()

    private fun createDataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost/weatherhistory"
        config.username = "postgres"
        config.password = "postgres"
        config.driverClassName = "org.postgresql.Driver"
        config.dataSourceProperties = Properties()
//        config.dataSourceProperties["reWriteBatchedInserts"] = "true"
//        config.dataSourceProperties["loggerLevel"] = "TRACE"
        return HikariDataSource(config)
    }
}

