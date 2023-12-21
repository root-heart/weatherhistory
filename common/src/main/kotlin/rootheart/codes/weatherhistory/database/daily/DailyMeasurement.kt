package rootheart.codes.weatherhistory.database.daily

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import rootheart.codes.weatherhistory.database.*
import java.math.BigDecimal
import java.util.*
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

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

    val sunshineMinutes = sum("SUNSHINE_DURATION_MINUTES", 6, 0)
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

    fun <T> fetchData(columns: Array<Column<*>>, stationId: Long, year: Int, mapper: (ResultRow) -> T): List<T> {
        val condition = this.stationId.eq(stationId).and(this.year.eq(year))
        val data = transaction {
            addLogger(StdOutSqlLogger)
            slice(columns.distinct()).select(condition).map(mapper)
        }
        return data
    }

    fun fetchMinAvgMaxData(columns: DailyMinAvgMaxColumns, stationId: Long, year: Int): List<MinAvgMaxData> {
        return fetchData(arrayOf(date, columns.min, columns.avg, columns.max), stationId, year) {
            MinAvgMaxData(
                    day = it[date].toDate(),
                    min = it[columns.min],
                    avg = it[columns.avg],
                    max = it[columns.max]
            )
        }
    }

    fun fetchSumData(columns: DailySumColums, stationId: Long, year: Int): List<SumData> {
        return fetchData(arrayOf(date, columns.sum), stationId, year) {
            SumData(it[date].toDate(), it[columns.sum])
        }
    }

    fun <T> fetchHistogramData(column: Column<Array<T?>?>, stationId: Long, year: Int): List<HistogramData<T>> {
        return fetchData(arrayOf(date, column), stationId, year) {
            HistogramData(it[date].toDate(), it[column])
        }
    }
}

class MinAvgMaxData(
        val day: Date,
        val min: BigDecimal?,
        val avg: BigDecimal?,
        val max: BigDecimal?
)

class SumData(
        val day: Date,
        val sum: BigDecimal?
)

class HistogramData<T>(
        val day: Date,
        val data: Array<T?>?
)

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

    fun toEntity(row: ResultRow): DailyMinMax {
        return DailyMinMax(min = row[min],
                           max = row[max],
                           details = row[details])
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

    fun toEntity(row: ResultRow): DailyAvgMax {
        return DailyAvgMax(avg = row[avg],
                           max = row[max],
                           details = row[details])
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

    fun toEntity(row: ResultRow): DailyMinAvgMax {
        return DailyMinAvgMax(min = row[min],
                              avg = row[avg],
                              max = row[max],
                              details = row[details])
    }
}

class DailySumColums(
        val sum: Column<BigDecimal?>,
        val details: Column<Array<BigDecimal?>?>
) {
    fun setValues(batch: BatchInsertStatement, values: DailySum) {
        batch[sum] = values.sum
        batch[details] = values.details
    }

    fun toEntity(row: ResultRow): DailySum {
        return DailySum(sum = row[sum], details = row[details])
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
    DailySumColums(
            decimal("SUM_$columnBaseName", precision, scale).nullable(),
            decimalArrayNullable("DETAILED_$columnBaseName"),
    )
