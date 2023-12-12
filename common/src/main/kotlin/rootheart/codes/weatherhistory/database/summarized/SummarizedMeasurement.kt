package rootheart.codes.weatherhistory.database.summarized

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.strings.splitAndTrimTokens
import rootheart.codes.common.strings.splitAndTrimTokensToList
import rootheart.codes.weatherhistory.database.StationsTable
import rootheart.codes.weatherhistory.database.intArray
import rootheart.codes.weatherhistory.database.intArrayNullable
import java.math.BigDecimal

object SummarizedMeasurementsTable : LongIdTable("SUMMARIZED_MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_SUMMARIZED_MEASUREMENT_STATION")
    val year = integer("YEAR")
    val month = integer("MONTH").nullable()
//    override val date = generatedDateColumn("DATE", "make_date(${year.name}, ${month.name}, 1)")

    val airTemperatureCentigrade = minAvgMax("AIR_TEMPERATURE_CENTIGRADE", 4, 1)
    val dewPointTemperatureCentigrade = minAvgMax("DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1)
    val humidityPercent = minAvgMax("HUMIDITY_PERCENT", 4, 1)
    val airPressureHectopascals = minAvgMax("AIR_PRESSURE_HECTOPASCALS", 6, 1)
    val visibilityMeters = minAvgMax("VISIBILITY_METERS", 6, 0)


    val sunshineMinutes = sum("SUNSHINE_DURATION_MINUTES", 6, 0)
    val rainfallMillimeters = sum("RAINFALL_MILLIMETERS", 6, 1)
    val snowfallMillimeters = sum("SNOWFALL_MILLIMETERS", 6, 1)

    val detailedCloudCoverage = detailedHistogram("DETAILED_CLOUD_COVERAGE")
    val cloudCoverageHistogram = intArray("CLOUD_COVERAGE_HISTOGRAM")
    val detailedWindDirectionDegrees = intArrayNullable("DETAILED_WIND_DIRECTION_DEGREES")

    val windSpeedMetersPerSecond = avgMax("WIND_SPEED_METERS_PER_SECOND", 4, 1)
}


private fun Table.minAvgMax(columnBaseName: String, precision: Int, scale: Int) =
        SummarizedMinAvgMaxColumns(
                decimal("MIN_$columnBaseName", precision, scale).nullable(),
                date("MIN_${columnBaseName}_DATE").nullable(),
                decimal("AVG_$columnBaseName", precision, scale).nullable(),
                decimal("MAX_$columnBaseName", precision, scale).nullable(),
                date("MAX_${columnBaseName}_DATE").nullable()
        )

private fun Table.avgMax(columnBaseName: String, precision: Int, scale: Int) =
        SummarizedAvgMaxColumns(
                decimal("AVG_$columnBaseName", precision, scale).nullable(),
                decimal("MAX_$columnBaseName", precision, scale).nullable(),
                date("MAX_${columnBaseName}_DATE").nullable()
        )

private fun Table.sum(columnBaseName: String, precision: Int, scale: Int) =
        SummarizedSumColumns(
                decimal("MIN_$columnBaseName", precision, scale).nullable(),
                date("MIN_${columnBaseName}_DATE").nullable(),
                decimal("MAX_$columnBaseName", precision, scale).nullable(),
                date("MAX_${columnBaseName}_DATE").nullable(),
                decimal("SUM_$columnBaseName", precision, scale).nullable()
        )

private fun Table.detailedHistogram(columnName: String): Column<Array<Array<Int>>?> =
        registerColumn(columnName, DetailedHistogramColumnType())

class SummarizedMinAvgMaxColumns(
        val min: Column<BigDecimal?>,
        val minDate: Column<DateTime?>,
        val avg: Column<BigDecimal?>,
        val max: Column<BigDecimal?>,
        val maxDate: Column<DateTime?>,
) {
    fun setValues(batch: BatchInsertStatement, values: SummarizedMinAvgMax) {
        batch[min] = values.min
//        batch[minDate] = values.minDate?.toDateTimeAtStartOfDay()
        batch[avg] = values.avg
        batch[max] = values.max
//        batch[maxDate] = values.maxDate?.toDateTimeAtStartOfDay()
    }

    fun toEntity(row: ResultRow): SummarizedMinAvgMax {
        return SummarizedMinAvgMax(
                min = row[min],
//                minDate = row[minDate]?.toLocalDate(),
                avg = row[avg],
                max = row[max],
//                maxDate = row[maxDate]?.toLocalDate(),
        )
    }
}

class SummarizedAvgMaxColumns(
        val avg: Column<BigDecimal?>,
        val max: Column<BigDecimal?>,
        val maxDate: Column<DateTime?>,
) {
    fun setValues(batch: BatchInsertStatement, values: SummarizedAvgMax) {
        batch[avg] = values.avg
        batch[max] = values.max
//        batch[maxDate] = values.maxDate?.toDateTimeAtStartOfDay()
    }

    fun toEntity(row: ResultRow): SummarizedAvgMax {
        return SummarizedAvgMax(
                avg = row[avg],
                max = row[max],
//                maxDate = row[maxDate]?.toLocalDate(),
        )
    }
}

class SummarizedSumColumns(
        val min: Column<BigDecimal?>,
        val minDate: Column<DateTime?>,
        val max: Column<BigDecimal?>,
        val maxDate: Column<DateTime?>,
        val sum: Column<BigDecimal?>
) {
    fun setValues(batch: BatchInsertStatement, values: SummarizedSum) {
        batch[min] = values.min
//        batch[minDate] = values.minDate?.toDateTimeAtStartOfDay()
        batch[max] = values.max
//        batch[maxDate] = values.maxDate?.toDateTimeAtStartOfDay()
        batch[sum] = values.sum
    }

    fun toEntity(row: ResultRow): SummarizedSum {
        return SummarizedSum(
                min = row[min],
//                minDate = row[minDate]?.toLocalDate(),
                max = row[max],
//                maxDate = row[maxDate]?.toLocalDate(),
                sum = row[sum]
        )
    }
}

class SummarizedMinAvgMax(
        var min: BigDecimal? = null,
//        var minDate: LocalDate? = null,
        var avg: BigDecimal? = null,
        var max: BigDecimal? = null,
//        var maxDate: LocalDate? = null
)

class SummarizedAvgMax(
        var avg: BigDecimal? = null,
        var max: BigDecimal? = null,
//        var maxDate: LocalDate? = null
)

class SummarizedSum(
        var min: BigDecimal? = null,
//        var minDate: LocalDate? = null,
        var max: BigDecimal? = null,
//        var maxDate: LocalDate? = null,
        var sum: BigDecimal? = null
)


class DetailedHistogramColumnType : ColumnType(true) {
    override fun sqlType(): String = "TEXT"

    override fun valueToDB(value: Any?): String? {
        if (value is Array<*>) {
            return value.map { it as Array<*> }
                    .joinToString("|") { it.joinToString(",") }
        }
        return null
    }

    override fun valueFromDB(value: Any): Array<Array<Int?>?> {
        if (value is String) {
            return splitAndTrimTokens(value, '|', "null", { s ->
                splitAndTrimTokens(s, ',', "null", { it.toInt() }).toTypedArray()
            }).toTypedArray()
        }
        return arrayOf()
    }

    override fun notNullValueToDB(value: Any): Any {
        if (value is Array<*>) {
            if (value.isEmpty()) {
                return ""
            }
            return value.joinToString(",")
        } else {
            return super.notNullValueToDB(value)
        }
    }
}
