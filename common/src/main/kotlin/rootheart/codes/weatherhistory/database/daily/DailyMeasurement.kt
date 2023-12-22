package rootheart.codes.weatherhistory.database.daily

import java.io.Serializable
import java.math.BigDecimal
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import rootheart.codes.weatherhistory.database.StationsTable
import rootheart.codes.weatherhistory.database.decimalArrayNullable
import rootheart.codes.weatherhistory.database.generatedDateColumn
import rootheart.codes.weatherhistory.database.intArray
import rootheart.codes.weatherhistory.database.intArrayNullable

object DailyMeasurementTable : LongIdTable("DAILY_MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_DETAILED_MEASUREMENT_STATION")
    val year = integer("YEAR")
    val month = integer("MONTH").nullable()
    val day = integer("DAY").nullable()
    val date = generatedDateColumn("DATE", "make_date(${year.name}, ${month.name}, ${day.name})")

    val airTemperatureCentigrade = minAvgMax("AIR_TEMPERATURE_CENTIGRADE", 4, 1)
    val dewPointTemperatureCentigrade = minAvgMax("DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1)
    val humidityPercent = minAvgMax("HUMIDITY_PERCENT", 4, 1)
    val airPressureHectopascals = minAvgMax("AIR_PRESSURE_HECTOPASCALS", 6, 1)

    val sunshineMinutes = sumInt("SUNSHINE_DURATION_MINUTES")
    val rainfallMillimeters = sum("RAINFALL_MILLIMETERS", 6, 1)
    val snowfallMillimeters = sum("SNOWFALL_MILLIMETERS", 6, 1)

    val detailedCloudCoverage = intArrayNullable("DETAILED_CLOUD_COVERAGE")
    val cloudCoverageHistogram = intArray("CLOUD_COVERAGE_HISTOGRAM")
    val windDirectionDegrees = minMax("WIND_DIRECTION_DEGREES", 3, 0)

    val windSpeedMetersPerSecond = avgMax("WIND_SPEED_METERS_PER_SECOND", 4, 1)
    val visibilityMeters = minAvgMax("VISIBILITY_METERS", 6, 0)

    init {
        index(isUnique = true, stationId, year, month, day)
    }

    fun <T> fetchData(
            columns: Array<Column<out Serializable?>>,
            stationId: Long,
            year: Int,
            mapper: (ResultRow) -> T
    ): List<T> {
        val condition = this.stationId.eq(stationId).and(this.year.eq(year))
        val data = transaction {
            addLogger(StdOutSqlLogger)
            slice(columns.distinct()).select(condition).map(mapper)
        }
        return data
    }
}


class DailyMinMaxColumns(
        val min: Column<BigDecimal?>,
        val max: Column<BigDecimal?>,
        val details: Column<Array<BigDecimal?>?>
) {
    fun setValues(batch: BatchInsertStatement, values: DailyMinMax) {
        batch[min] = values.min
        batch[max] = values.max
        batch[details] = values.details
    }
}

class DailyAvgMaxColumns(
        val avg: Column<BigDecimal?>,
        val max: Column<BigDecimal?>,
        val details: Column<Array<BigDecimal?>?>
) {
    fun setValues(batch: BatchInsertStatement, values: DailyAvgMax) {
        batch[avg] = values.avg
        batch[max] = values.max
        batch[details] = values.details
    }
}

class DailyMinAvgMaxColumns(
        val min: Column<BigDecimal?>,
        val avg: Column<BigDecimal?>,
        val max: Column<BigDecimal?>,
        val details: Column<Array<BigDecimal?>?>
) {
    fun setValues(batch: BatchInsertStatement, values: DailyMinAvgMax) {
        batch[min] = values.min
        batch[avg] = values.avg
        batch[max] = values.max
        batch[details] = values.details
    }
}

class DailySumColumns<T>(
        val sum: Column<T?>,
        val details: Column<Array<T?>?>
) where T : Serializable, T : Comparable<T> {
    fun setValues(batch: BatchInsertStatement, values: DailySum<T>) {
        batch[sum] = values.sum
        batch[details] = values.details
    }
}

private fun Table.minAvgMax(columnBaseName: String, precision: Int, scale: Int) =
    DailyMinAvgMaxColumns(
            decimal("MIN_$columnBaseName", precision, scale).nullable(),
            decimal("AVG_$columnBaseName", precision, scale).nullable(),
            decimal("MAX_$columnBaseName", precision, scale).nullable(),
            decimalArrayNullable("DETAILED_$columnBaseName"),
    )

private fun Table.minMax(columnBaseName: String, precision: Int, scale: Int) =
    DailyMinMaxColumns(
            decimal("MIN_$columnBaseName", precision, scale).nullable(),
            decimal("MAX_$columnBaseName", precision, scale).nullable(),
            decimalArrayNullable("DETAILED_$columnBaseName"),
    )

private fun Table.avgMax(columnBaseName: String, precision: Int, scale: Int) =
    DailyAvgMaxColumns(
            decimal("AVG_$columnBaseName", precision, scale).nullable(),
            decimal("MAX_$columnBaseName", precision, scale).nullable(),
            decimalArrayNullable("DETAILED_$columnBaseName")
    )

private fun Table.sum(columnBaseName: String, precision: Int, scale: Int) =
    DailySumColumns(
            decimal("SUM_$columnBaseName", precision, scale).nullable(),
            decimalArrayNullable("DETAILED_$columnBaseName"),
    )

private fun Table.sumInt(columnBaseName: String) =
    DailySumColumns(
            integer("SUM_$columnBaseName").nullable(),
            intArrayNullable("DETAILED_$columnBaseName"),
    )
