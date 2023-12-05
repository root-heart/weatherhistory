package rootheart.codes.weatherhistory.database.daily

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import rootheart.codes.weatherhistory.database.*
import java.math.BigDecimal

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

    fun toEntity(row: ResultRow): DailyMeasurementEntity {
        val measurements = DailyMeasurements(
                airTemperatureCentigrade = airTemperatureCentigrade.toEntity(row),
                dewPointTemperatureCentigrade = dewPointTemperatureCentigrade.toEntity(row),
                humidityPercent = humidityPercent.toEntity(row),
                airPressureHectopascals = airPressureHectopascals.toEntity(row),
                sunshineMinutes = sunshineMinutes.toEntity(row),
                rainfallMillimeters = rainfallMillimeters.toEntity(row),
                snowfallMillimeters = snowfallMillimeters.toEntity(row),
                windSpeedMetersPerSecond = windSpeedMetersPerSecond.toEntity(row),
                visibilityMeters = visibilityMeters.toEntity(row),
                windDirectionDegrees = windDirectionDegrees.toEntity(row),
                detailedCloudCoverage = row[detailedCloudCoverage],
                cloudCoverageHistogram = row[cloudCoverageHistogram])
        return DailyMeasurementEntity(stationId = row[stationId].value,
                                      date = row[date].toLocalDate(),
                                      measurements = measurements)
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
