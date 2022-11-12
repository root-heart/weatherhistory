package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty1
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val log = KotlinLogging.logger { }

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val day = date("DAY")

    val hourlyAirTemperatureCentigrade = array<BigDecimal>("HOURLY_AIR_TEMPERATURE_CENTIGRADE", DecimalColumnType(4, 1))
    val minAirTemperatureCentigrade = decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgAirTemperatureCentigrade = decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxAirTemperatureCentigrade = decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val hourlyDewPointTemperatureCentigrade =
        array<BigDecimal>("HOURLY_DEW_POINT_TEMPERATURE_CENTIGRADE", DecimalColumnType(4, 1))
    val minDewPointTemperatureCentigrade = decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgDewPointTemperatureCentigrade = decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxDewPointTemperatureCentigrade = decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val hourlyHumidityPercent = array<BigDecimal>("HOURLY_HUMIDITY_PERCENT", DecimalColumnType(4, 1))
    val minHumidityPercent = decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable()
    val avgHumidityPercent = decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable()
    val maxHumidityPercent = decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable()

    val hourlyAirPressureHectopascals = array<BigDecimal>("HOURLY_AIR_PRESSURE_HECTOPASCALS", DecimalColumnType(5, 1))
    val minAirPressureHectopascals = decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val avgAirPressureHectopascals = decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val maxAirPressureHectopascals = decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()

    val hourlyCloudCoverage = array<Int>("HOURLY_CLOUD_COVERAGE", IntegerColumnType())

    val hourlySunshineDurationMinutes = array<Int>("HOURLY_SUNSHINE_DURATION_MINUTES", IntegerColumnType())
    val sumSunshineDurationHours = decimal("SUM_SUNSHINE_DURATION_HOURS", 8, 1).nullable()

    val hourlyRainfallMillimeters = array<BigDecimal>("HOURLY_RAINFALL_MILLIMETERS", DecimalColumnType(4, 1))
    val sumRainfallMillimeters = decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable()

    val hourlySnowfallMillimeters = array<BigDecimal>("HOURLY_SNOWFALL_MILLIMETERS", DecimalColumnType(4, 1))
    val sumSnowfallMillimeters = decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable()

    val hourlyWindSpeedMetersPerSecond =
        array<BigDecimal>("HOURLY_WIND_SPEED_METERS_PER_SECOND", DecimalColumnType(4, 1))
    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val avgWindSpeedMetersPerSecond = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()

    val hourlyWindDirectionDegrees = array<Int>("HOURLY_WIND_DIRECTION_DEGREES", IntegerColumnType())

    val hourlyVisibilityMeters = array<Int>("HOURLY_VISIBILITY_METERS", IntegerColumnType())

    init {
        index(isUnique = true, stationId, day);
    }
}

object MeasurementTableMapping : TableMapping<Measurement>(
    Measurement::stationIdLong to MeasurementsTable.stationId,
    Measurement::dayDateTime to MeasurementsTable.day,

    Measurement::hourlyAirTemperatureCentigrade to MeasurementsTable.hourlyAirTemperatureCentigrade,
    Measurement::minAirTemperatureCentigrade to MeasurementsTable.minAirTemperatureCentigrade,
    Measurement::avgAirTemperatureCentigrade to MeasurementsTable.avgAirTemperatureCentigrade,
    Measurement::maxAirTemperatureCentigrade to MeasurementsTable.maxAirTemperatureCentigrade,

    Measurement::hourlyDewPointTemperatureCentigrade to MeasurementsTable.hourlyDewPointTemperatureCentigrade,
    Measurement::minDewPointTemperatureCentigrade to MeasurementsTable.minDewPointTemperatureCentigrade,
    Measurement::avgDewPointTemperatureCentigrade to MeasurementsTable.avgDewPointTemperatureCentigrade,
    Measurement::maxDewPointTemperatureCentigrade to MeasurementsTable.maxDewPointTemperatureCentigrade,

    Measurement::hourlyHumidityPercent to MeasurementsTable.hourlyHumidityPercent,
    Measurement::minHumidityPercent to MeasurementsTable.minHumidityPercent,
    Measurement::avgHumidityPercent to MeasurementsTable.avgHumidityPercent,
    Measurement::maxHumidityPercent to MeasurementsTable.maxHumidityPercent,

    Measurement::hourlyAirPressureHectopascals to MeasurementsTable.hourlyAirPressureHectopascals,
    Measurement::minAirPressureHectopascals to MeasurementsTable.minAirPressureHectopascals,
    Measurement::avgAirPressureHectopascals to MeasurementsTable.avgAirPressureHectopascals,
    Measurement::maxAirPressureHectopascals to MeasurementsTable.maxAirPressureHectopascals,

    Measurement::hourlyCloudCoverages to MeasurementsTable.hourlyCloudCoverage,

    Measurement::hourlySunshineDurationMinutes to MeasurementsTable.hourlySunshineDurationMinutes,
    Measurement::sumSunshineDurationHours to MeasurementsTable.sumSunshineDurationHours,

    Measurement::hourlyRainfallMillimeters to MeasurementsTable.hourlyRainfallMillimeters,
    Measurement::sumRainfallMillimeters to MeasurementsTable.sumRainfallMillimeters,

    Measurement::hourlySnowfallMillimeters to MeasurementsTable.hourlySnowfallMillimeters,
    Measurement::sumSnowfallMillimeters to MeasurementsTable.sumSnowfallMillimeters,

    Measurement::hourlyWindSpeedMetersPerSecond to MeasurementsTable.hourlyWindSpeedMetersPerSecond,
    Measurement::maxWindSpeedMetersPerSecond to MeasurementsTable.maxWindSpeedMetersPerSecond,
    Measurement::avgWindSpeedMetersPerSecond to MeasurementsTable.avgWindSpeedMetersPerSecond,

    Measurement::hourlyWindDirectionDegrees to MeasurementsTable.hourlyWindDirectionDegrees,

    Measurement::hourlyVisibilityMeters to MeasurementsTable.hourlyVisibilityMeters,
)

class Measurement(
    val station: Station,
    var day: LocalDate,

    var hourlyAirTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,

    var hourlyDewPointTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,

    var hourlyHumidityPercent: Array<BigDecimal?> = Array(24) { null },
    var minHumidityPercent: BigDecimal? = null,
    var maxHumidityPercent: BigDecimal? = null,
    var avgHumidityPercent: BigDecimal? = null,

    var hourlyAirPressureHectopascals: Array<BigDecimal?> = Array(24) { null },
    var minAirPressureHectopascals: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var maxAirPressureHectopascals: BigDecimal? = null,

    var hourlyCloudCoverages: Array<Int?> = Array(24) { null },

    var hourlySunshineDurationMinutes: Array<Int?> = Array(24) { null },
    var sumSunshineDurationHours: BigDecimal? = null,

    var hourlyRainfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumRainfallMillimeters: BigDecimal? = null,

    var hourlySnowfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumSnowfallMillimeters: BigDecimal? = null,

    var hourlyWindSpeedMetersPerSecond: Array<BigDecimal?> = Array(24) { null },
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,

    var hourlyWindDirectionDegrees: Array<Int?> = Array(24) { null },

    var hourlyVisibilityMeters: Array<Int?> = Array(24) { null },
) {
    val stationIdLong get() = station.id
    val dayDateTime get() = day.toDateTimeAtStartOfDay()!!
}

@OptIn(ExperimentalTime::class)
object MeasurementDao {
    fun findByStationIdAndYear(station: Station, year: Int): List<Measurement> = transaction {
        val timedValue = measureTimedValue {
            val start = DateTime(year, 1, 1, 0, 0)
            val end = DateTime(year + 1, 1, 1, 0, 0)
            MeasurementsTable.select {
                MeasurementsTable.stationId.eq(station.id!!)
                    .and(MeasurementsTable.day.greaterEq(start))
                    .and(MeasurementsTable.day.less(end))
            }
                .map { toMeasurement(station, it) }
        }
        log.info { "findByStationIdAndYear(${station.id}, $year) took ${timedValue.duration.inWholeMilliseconds} millis" }
        return@transaction timedValue.value
    }

    private fun toMeasurement(station: Station, row: ResultRow): Measurement {
        val hourlyMeasurement = createMeasurement(station, row)
        setValuesFromResultRow(row, hourlyMeasurement)
        return hourlyMeasurement
    }

    private fun createMeasurement(station: Station, row: ResultRow): Measurement {
        return Measurement(station = station, day = row[MeasurementsTable.day].toLocalDate())
    }

    private fun setValuesFromResultRow(row: ResultRow, hourlyMeasurement: Measurement) {
        for (mapping in MeasurementTableMapping.mappings) {
            val property = mapping.first
            if (property is KMutableProperty1) {
                property.set(hourlyMeasurement, row[mapping.second])
            }
        }
    }
}

fun <T> Table.array(name: String, columnType: ColumnType): Column<Array<T>> =
    registerColumn(name, ArrayColumnType(columnType))

class ArrayColumnType(private val type: ColumnType) : ColumnType() {

    override fun sqlType(): String = buildString {
        append(type.sqlType())
        append(" ARRAY")
    }

    override fun valueToDB(value: Any?): Any? {
        return if (value is Array<*>) {
            val columnType = type.sqlType().split("(")[0]
            val jdbcConnection = TransactionManager.current().connection
            jdbcConnection.createArrayOf(columnType, value)
        } else {
            super.valueToDB(value)
        }
    }

    override fun valueFromDB(value: Any): Any {
        if (value is java.sql.Array) {
            return value.array
        }
        if (value is Array<*>) {
            return value
        }
        error("Array does not support for this database")
    }

    override fun notNullValueToDB(value: Any): Any {
        if (value is Array<*>) {
            if (value.isEmpty())
                return "'{}'"

            val columnType = type.sqlType().split("(")[0]
            val jdbcConnection = TransactionManager.current().connection
            return jdbcConnection.createArrayOf(columnType, value) ?: error("Can't create non null array for $value")
        } else {
            return super.notNullValueToDB(value)
        }
    }
}