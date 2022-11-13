package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.measureAndLogDuration
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty1
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val log = KotlinLogging.logger { }

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val day = date("DAY")

    val hourlyAirTemperatureCentigrade =
        array<BigDecimal?>("HOURLY_AIR_TEMPERATURE_CENTIGRADE", DecimalColumnType(4, 1))
    val minAirTemperatureCentigrade = decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgAirTemperatureCentigrade = decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxAirTemperatureCentigrade = decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val hourlyDewPointTemperatureCentigrade =
        array<BigDecimal?>("HOURLY_DEW_POINT_TEMPERATURE_CENTIGRADE", DecimalColumnType(4, 1))
    val minDewPointTemperatureCentigrade = decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgDewPointTemperatureCentigrade = decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxDewPointTemperatureCentigrade = decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val hourlyHumidityPercent = array<BigDecimal?>("HOURLY_HUMIDITY_PERCENT", DecimalColumnType(4, 1))
    val minHumidityPercent = decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable()
    val avgHumidityPercent = decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable()
    val maxHumidityPercent = decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable()

    val hourlyAirPressureHectopascals = array<BigDecimal?>("HOURLY_AIR_PRESSURE_HECTOPASCALS", DecimalColumnType(5, 1))
    val minAirPressureHectopascals = decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val avgAirPressureHectopascals = decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val maxAirPressureHectopascals = decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()

    val hourlyCloudCoverage = array<Int?>("HOURLY_CLOUD_COVERAGE", IntegerColumnType())

    val hourlySunshineDurationMinutes = array<Int?>("HOURLY_SUNSHINE_DURATION_MINUTES", IntegerColumnType())
    val sumSunshineDurationHours = decimal("SUM_SUNSHINE_DURATION_HOURS", 8, 1).nullable()

    val hourlyRainfallMillimeters = array<BigDecimal?>("HOURLY_RAINFALL_MILLIMETERS", DecimalColumnType(4, 1))
    val sumRainfallMillimeters = decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable()

    val hourlySnowfallMillimeters = array<BigDecimal?>("HOURLY_SNOWFALL_MILLIMETERS", DecimalColumnType(4, 1))
    val sumSnowfallMillimeters = decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable()

    val hourlyWindSpeedMetersPerSecond =
        array<BigDecimal?>("HOURLY_WIND_SPEED_METERS_PER_SECOND", DecimalColumnType(4, 1))
    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val avgWindSpeedMetersPerSecond = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()

    val hourlyWindDirectionDegrees = array<Int?>("HOURLY_WIND_DIRECTION_DEGREES", IntegerColumnType())

    val hourlyVisibilityMeters = array<Int?>("HOURLY_VISIBILITY_METERS", IntegerColumnType())

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

object MeasurementDao {
    fun findByStationIdAndYear(station: Station, year: Int): List<Measurement> = transaction {
        measureAndLogDuration("MeasurementDao.findByStationIdAndYear(${station.id}, $year)") {
            val resultRows = selectMeasurementsByYear(station, year)
            map(station, resultRows)
        }
    }

    private fun selectMeasurementsByYear(station: Station, year: Int): Iterable<ResultRow> =
        measureAndLogDuration("MeasurementDao.selectMeasurementsByYear(${station.id}, $year)") {
            val start = DateTime(year, 1, 1, 0, 0)
            val end = DateTime(year + 1, 1, 1, 0, 0)
            MeasurementsTable
                .select {
                    MeasurementsTable.stationId.eq(station.id)
                        .and(MeasurementsTable.day.greaterEq(start))
                        .and(MeasurementsTable.day.less(end))
                }
                .fetchSize(500)
                .toList()
        }

    private fun map(station: Station, resultRows: Iterable<ResultRow>): List<Measurement> =
        measureAndLogDuration("MeasurementDao.map(${station.id})") {
            resultRows.map { toMeasurement(station, it) }
        }

    // TODO mapping the arrays slows down the mapping speed by a factor of about ten
    // perhaps do not use PG arrays for storing array values
    private fun toMeasurement(station: Station, row: ResultRow) =
        Measurement(
            station = station,
            day = row[MeasurementsTable.day].toLocalDate(),

            // TODO find alternative solution
            hourlyAirTemperatureCentigrade = row[MeasurementsTable.hourlyAirTemperatureCentigrade],
            minAirTemperatureCentigrade = row[MeasurementsTable.minAirTemperatureCentigrade],
            avgAirTemperatureCentigrade = row[MeasurementsTable.avgAirTemperatureCentigrade],
            maxAirTemperatureCentigrade = row[MeasurementsTable.maxAirTemperatureCentigrade],

            // TODO find alternative solution
            hourlyDewPointTemperatureCentigrade = row[MeasurementsTable.hourlyDewPointTemperatureCentigrade],
            minDewPointTemperatureCentigrade = row[MeasurementsTable.minDewPointTemperatureCentigrade],
            avgDewPointTemperatureCentigrade = row[MeasurementsTable.avgDewPointTemperatureCentigrade],
            maxDewPointTemperatureCentigrade = row[MeasurementsTable.maxDewPointTemperatureCentigrade],

            // TODO find alternative solution
            hourlyHumidityPercent = row[MeasurementsTable.hourlyHumidityPercent],
            minHumidityPercent = row[MeasurementsTable.minHumidityPercent],
            avgHumidityPercent = row[MeasurementsTable.avgHumidityPercent],
            maxHumidityPercent = row[MeasurementsTable.maxHumidityPercent],

            // TODO find alternative solution
            hourlyAirPressureHectopascals = row[MeasurementsTable.hourlyAirPressureHectopascals],
            minAirPressureHectopascals = row[MeasurementsTable.minAirPressureHectopascals],
            avgAirPressureHectopascals = row[MeasurementsTable.avgAirPressureHectopascals],
            maxAirPressureHectopascals = row[MeasurementsTable.maxAirPressureHectopascals],

            // TODO find alternative solution
            hourlyCloudCoverages = row[MeasurementsTable.hourlyCloudCoverage],

            // TODO find alternative solution
            hourlySunshineDurationMinutes = row[MeasurementsTable.hourlySunshineDurationMinutes],
            sumSunshineDurationHours = row[MeasurementsTable.sumSunshineDurationHours],

            // TODO find alternative solution
            hourlyRainfallMillimeters = row[MeasurementsTable.hourlyRainfallMillimeters],
            sumRainfallMillimeters = row[MeasurementsTable.sumRainfallMillimeters],

            // TODO find alternative solution
            hourlySnowfallMillimeters = row[MeasurementsTable.hourlySnowfallMillimeters],
            sumSnowfallMillimeters = row[MeasurementsTable.sumSnowfallMillimeters],

            // TODO find alternative solution
            hourlyWindSpeedMetersPerSecond = row[MeasurementsTable.hourlyWindSpeedMetersPerSecond],
            maxWindSpeedMetersPerSecond = row[MeasurementsTable.maxWindSpeedMetersPerSecond],
            avgWindSpeedMetersPerSecond = row[MeasurementsTable.avgWindSpeedMetersPerSecond],

            // TODO find alternative solution
            hourlyWindDirectionDegrees = row[MeasurementsTable.hourlyWindDirectionDegrees],

            // TODO find alternative solution
            hourlyVisibilityMeters = row[MeasurementsTable.hourlyVisibilityMeters],
        )
}

// TODO find another solution for storing array values that is faster than PGARRAY
fun <T> Table.array(name: String, columnType: ColumnType): Column<Array<T>> =
    registerColumn(name, ArrayColumnType(columnType))

class ArrayColumnType(private val type: ColumnType) : ColumnType() {

    override fun sqlType(): String = "VARCHAR(200)"

    override fun valueToDB(value: Any?): Any? {
        return if (value is Array<*>) {
            value.joinToString(",")
        } else {
            super.valueToDB(value)
        }
    }

    override fun valueFromDB(value: Any): Any {
        if (value is String) {
            if (type is DecimalColumnType) {
//                return Array<BigDecimal?>(24) { null }
                return value.split(',').map { if (it == "null") null else BigDecimal(it) }.toTypedArray()
            } else if (type is IntegerColumnType) {
//                return Array<Int?>(24) { null }
                return value.split(',').map { if (it == "null") null else Integer.parseInt(it) }.toTypedArray()
            }
        }
        error("Array does not support for this database")
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