package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.common.strings.splitAndTrimTokensToArrayWithLength24
import java.math.BigDecimal
import java.sql.Date
import java.sql.ResultSet
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty1

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
    val hourlyWindDirectionDegrees = array<Int?>("HOURLY_WIND_DIRECTION_DEGREES", IntegerColumnType())
    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val avgWindSpeedMetersPerSecond = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()


    val hourlyVisibilityMeters = array<Int?>("HOURLY_VISIBILITY_METERS", IntegerColumnType())

    init {
        index(isUnique = true, stationId, day)
    }

    fun byStationIdAndDateBetween(stationId: Long, start: LocalDate, end: LocalDate) =
        MeasurementsTable.stationId.eq(stationId)
            .and(day.between(start.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay()))
}

object MeasurementTableMapping : TableMapping<Measurement>(
    Measurement::stationId to MeasurementsTable.stationId,
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
    Measurement::hourlyWindDirectionDegrees to MeasurementsTable.hourlyWindDirectionDegrees,
    Measurement::maxWindSpeedMetersPerSecond to MeasurementsTable.maxWindSpeedMetersPerSecond,
    Measurement::avgWindSpeedMetersPerSecond to MeasurementsTable.avgWindSpeedMetersPerSecond,


    Measurement::hourlyVisibilityMeters to MeasurementsTable.hourlyVisibilityMeters,
)

class Measurement(
    @Transient var station: Station,
    @Transient var day: LocalDate,

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
    var hourlyWindDirectionDegrees: Array<Int?> = Array(24) { null },
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,


    var hourlyVisibilityMeters: Array<Int?> = Array(24) { null },
) {
    val stationId get() = station.id
    val dayDateTime get() = day.toDateTimeAtStartOfDay()!!
    val dayFormatted get() = day.toString("yyyy-MM-dd")
}

val MeasurementDao = MeasurementDaoJdbc

object MeasurementDaoExposed {
    // takes about 6 milliseconds most of the time, sometimes a bit over 7 milliseconds and sometimes a bit under 5
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
        }

    private fun map(station: Station, resultRows: Iterable<ResultRow>): List<Measurement> =
        measureAndLogDuration("MeasurementDao.map(${station.id})") {
            resultRows.map { toMeasurement(station, it) }
        }

    private fun toMeasurement(station: Station, row: ResultRow) =
        Measurement(
            station = station,
            day = row[MeasurementsTable.day].toLocalDate(),

            hourlyAirTemperatureCentigrade = row[MeasurementsTable.hourlyAirTemperatureCentigrade],
            minAirTemperatureCentigrade = row[MeasurementsTable.minAirTemperatureCentigrade],
            avgAirTemperatureCentigrade = row[MeasurementsTable.avgAirTemperatureCentigrade],
            maxAirTemperatureCentigrade = row[MeasurementsTable.maxAirTemperatureCentigrade],

            hourlyDewPointTemperatureCentigrade = row[MeasurementsTable.hourlyDewPointTemperatureCentigrade],
            minDewPointTemperatureCentigrade = row[MeasurementsTable.minDewPointTemperatureCentigrade],
            avgDewPointTemperatureCentigrade = row[MeasurementsTable.avgDewPointTemperatureCentigrade],
            maxDewPointTemperatureCentigrade = row[MeasurementsTable.maxDewPointTemperatureCentigrade],

            hourlyHumidityPercent = row[MeasurementsTable.hourlyHumidityPercent],
            minHumidityPercent = row[MeasurementsTable.minHumidityPercent],
            avgHumidityPercent = row[MeasurementsTable.avgHumidityPercent],
            maxHumidityPercent = row[MeasurementsTable.maxHumidityPercent],

            hourlyAirPressureHectopascals = row[MeasurementsTable.hourlyAirPressureHectopascals],
            minAirPressureHectopascals = row[MeasurementsTable.minAirPressureHectopascals],
            avgAirPressureHectopascals = row[MeasurementsTable.avgAirPressureHectopascals],
            maxAirPressureHectopascals = row[MeasurementsTable.maxAirPressureHectopascals],

            hourlyCloudCoverages = row[MeasurementsTable.hourlyCloudCoverage],

            hourlySunshineDurationMinutes = row[MeasurementsTable.hourlySunshineDurationMinutes],
            sumSunshineDurationHours = row[MeasurementsTable.sumSunshineDurationHours],

            hourlyRainfallMillimeters = row[MeasurementsTable.hourlyRainfallMillimeters],
            sumRainfallMillimeters = row[MeasurementsTable.sumRainfallMillimeters],

            hourlySnowfallMillimeters = row[MeasurementsTable.hourlySnowfallMillimeters],
            sumSnowfallMillimeters = row[MeasurementsTable.sumSnowfallMillimeters],

            hourlyWindSpeedMetersPerSecond = row[MeasurementsTable.hourlyWindSpeedMetersPerSecond],
            hourlyWindDirectionDegrees = row[MeasurementsTable.hourlyWindDirectionDegrees],
            maxWindSpeedMetersPerSecond = row[MeasurementsTable.maxWindSpeedMetersPerSecond],
            avgWindSpeedMetersPerSecond = row[MeasurementsTable.avgWindSpeedMetersPerSecond],

            hourlyVisibilityMeters = row[MeasurementsTable.hourlyVisibilityMeters],
        )
}

object MeasurementDaoJdbc {
    // takes 4 to 5 milliseconds most of the time, sometimes a bit over 6 milliseconds and sometimes a bit under 4
    fun findByStationIdAndYear(station: Station, year: Int): List<Measurement> = transaction {
        measureAndLogDuration("MeasurementDao.findByStationIdAndYear(${station.id}, $year)") {
            val start = DateTime(year, 1, 1, 0, 0)
            val end = start.plusYears(1)
            selectMeasurementsByDayBetween(station, start, end)
        }
    }

    fun findByStationIdAndYearAndMonth(station: Station, year: Int, month: Int): List<Measurement> = transaction {
        measureAndLogDuration("MeasurementDao.findByStationIdAndYear(${station.id}, $year)") {
            val start = DateTime(year, month, 1, 0, 0)
            val end = start.plusMonths(1)
            selectMeasurementsByDayBetween(station, start, end)
        }
    }

    private fun selectMeasurementsByDayBetween(station: Station, start: DateTime, end: DateTime): List<Measurement> =
        measureAndLogDuration("MeasurementDao.selectMeasurementsByYear(${station.id}, $start, $end)") {
            val sql = "select * from measurements where station_id = ? and \"day\" >= ? and \"day\" < ?"
            val measurements = ArrayList<Measurement>()
            WeatherDb.dataSource.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, station.id!!)
                    stmt.setDate(2, Date(start.millis))
                    stmt.setDate(3, Date(end.millis))
                    stmt.fetchSize = 500
                    stmt.executeQuery().use { rs ->
                        measureAndLogDuration("toMeasurement(${station.id}, resultSet)") {
                            while (rs.next()) {
                                measurements += toMeasurement(station, rs)
                            }
                        }
                    }
                }
            }
            return@measureAndLogDuration measurements
        }

    private fun toMeasurement(station: Station, rs: ResultSet) = Measurement(
        station = station,
        day = LocalDate(rs.getDate("day")),

        hourlyAirTemperatureCentigrade = rs.getBigDecimalArray24(MeasurementsTable.hourlyAirTemperatureCentigrade.name),
        minAirTemperatureCentigrade = rs.getBigDecimal(MeasurementsTable.minAirTemperatureCentigrade.name),
        avgAirTemperatureCentigrade = rs.getBigDecimal(MeasurementsTable.avgAirTemperatureCentigrade.name),
        maxAirTemperatureCentigrade = rs.getBigDecimal(MeasurementsTable.maxAirTemperatureCentigrade.name),

        hourlyDewPointTemperatureCentigrade = rs.getBigDecimalArray24(MeasurementsTable.hourlyDewPointTemperatureCentigrade.name),
        minDewPointTemperatureCentigrade = rs.getBigDecimal(MeasurementsTable.minDewPointTemperatureCentigrade.name),
        avgDewPointTemperatureCentigrade = rs.getBigDecimal(MeasurementsTable.avgDewPointTemperatureCentigrade.name),
        maxDewPointTemperatureCentigrade = rs.getBigDecimal(MeasurementsTable.maxDewPointTemperatureCentigrade.name),

        hourlyHumidityPercent = rs.getBigDecimalArray24(MeasurementsTable.hourlyHumidityPercent.name),
        minHumidityPercent = rs.getBigDecimal(MeasurementsTable.minHumidityPercent.name),
        avgHumidityPercent = rs.getBigDecimal(MeasurementsTable.avgHumidityPercent.name),
        maxHumidityPercent = rs.getBigDecimal(MeasurementsTable.maxHumidityPercent.name),

        hourlyAirPressureHectopascals = rs.getBigDecimalArray24(MeasurementsTable.hourlyAirPressureHectopascals.name),
        minAirPressureHectopascals = rs.getBigDecimal(MeasurementsTable.minAirPressureHectopascals.name),
        avgAirPressureHectopascals = rs.getBigDecimal(MeasurementsTable.avgAirPressureHectopascals.name),
        maxAirPressureHectopascals = rs.getBigDecimal(MeasurementsTable.maxAirPressureHectopascals.name),

        hourlyCloudCoverages = rs.getIntArray24(MeasurementsTable.hourlyCloudCoverage.name),

        hourlySunshineDurationMinutes = rs.getIntArray24(MeasurementsTable.hourlySunshineDurationMinutes.name),
        sumSunshineDurationHours = rs.getBigDecimal(MeasurementsTable.sumSunshineDurationHours.name),

        hourlyRainfallMillimeters = rs.getBigDecimalArray24(MeasurementsTable.hourlyRainfallMillimeters.name),
        sumRainfallMillimeters = rs.getBigDecimal(MeasurementsTable.sumRainfallMillimeters.name),

        hourlySnowfallMillimeters = rs.getBigDecimalArray24(MeasurementsTable.hourlySnowfallMillimeters.name),
        sumSnowfallMillimeters = rs.getBigDecimal(MeasurementsTable.sumSnowfallMillimeters.name),

        hourlyWindSpeedMetersPerSecond = rs.getBigDecimalArray24(MeasurementsTable.hourlyWindSpeedMetersPerSecond.name),
        maxWindSpeedMetersPerSecond = rs.getBigDecimal(MeasurementsTable.maxWindSpeedMetersPerSecond.name),
        avgWindSpeedMetersPerSecond = rs.getBigDecimal(MeasurementsTable.avgWindSpeedMetersPerSecond.name),

        hourlyWindDirectionDegrees = rs.getIntArray24(MeasurementsTable.hourlyWindDirectionDegrees.name),

        hourlyVisibilityMeters = rs.getIntArray24(MeasurementsTable.hourlyVisibilityMeters.name),
    )

}

data class TemperatureMeasurementJson(
    var day: String = "",
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,
) {
    var dayAsDateTime: DateTime
        get() = DateTime.parse(day, DateTimeFormat.forPattern(DATE_TIME_PATTERN))
        set(value) {
            day = LocalDate(value).toString(DATE_TIME_PATTERN)
        }
}

private val DATE_TIME_PATTERN = "yyyy-MM-dd"

data class PropertyColumnPair<O, D>(
    val property: KMutableProperty1<O, in D>,
    val column: Column<out D>,
    val resultSetGetter: KFunction2<ResultSet, String, D>
) {
    fun setValueFromResultRow(r: ResultRow, o: O) = property.set(o, r[column])
    fun setValueFromResultSet(r: ResultSet, o: O) {
        property.set(o, resultSetGetter(r, column.name))
    }
}

fun ResultSet.getBigDecimalByName(columnName: String) = getBigDecimal(columnName)
fun ResultSet.getDateTimeByName(columnName: String) = DateTime(getDate(columnName))


abstract class Dao<JsonType>(
    val jsonConstructor: () -> JsonType,
    private vararg val columns: PropertyColumnPair<JsonType, *>
) {
    private val sql = "select ${columns.joinToString(",") { it.column.name }} " +
            "from measurements " +
            "where ${MeasurementsTable.stationId.name} = ? " +
            "and ${MeasurementsTable.day.name} >= ? " +
            "and ${MeasurementsTable.day.name} < ?"

    private val fieldSet = MeasurementsTable.slice(columns.map { it.column }.toList())

    fun findDailyByYear(station: Station, year: Int): List<JsonType> =
        measureAndLogDuration("TemperatureMeasurementDao.findDailyByYear(${station.id}, $year)") {
            val start = LocalDate(year, 1, 1)
            val end = start.plusYears(1)
            return@measureAndLogDuration fetchFromDb(station.id!!, start, end)
        }

    fun findDailyByYearAndMonth(station: Station, year: Int, month: Int): List<JsonType> =
        measureAndLogDuration("TemperatureMeasurementDao.findDailyByYearAndMonth(${station.id}, $year, $month)") {
            val start = LocalDate(year, month, 1)
            val end = start.plusMonths(1)
            return@measureAndLogDuration fetchFromDb(station.id!!, start, end)
        }

    private fun fetchFromDb(stationId: Long, start: LocalDate, end: LocalDate): List<JsonType> = transaction {
//        val x = fieldSet
//            .select { MeasurementsTable.byStationIdAndDateBetween(stationId, start, end) }
//        return@transaction measureAndLogDuration("create json($stationId, resultSet)") {
//            x.map(::buildJsonType)
//        }
        WeatherDb.dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, stationId)
                stmt.setDate(2, Date(start.toDate().time))
                stmt.setDate(3, Date(end.toDate().time))
                stmt.executeQuery().use { rs ->
                    measureAndLogDuration("create json($stationId, resultSet)") {
                        val measurements = ArrayList<JsonType>()
                        while (rs.next()) {
                            measurements += buildJsonType(rs)
                        }
                        return@measureAndLogDuration measurements
                    }
                }
            }
        }
    }

    private fun buildJsonType(resultRow: ResultRow): JsonType {
        val json = jsonConstructor()
        columns.forEach { it.setValueFromResultRow(resultRow, json) }
        return json
    }

    private fun buildJsonType(rs: ResultSet): JsonType {
        val json = jsonConstructor()
        columns.forEach { it.setValueFromResultSet(rs, json) }
        return json
    }
}

object TemperatureMeasurementDao : Dao<TemperatureMeasurementJson>(
    ::TemperatureMeasurementJson,
    PropertyColumnPair(
        TemperatureMeasurementJson::dayAsDateTime, MeasurementsTable.day,
        ResultSet::getDateTimeByName
    ),
    PropertyColumnPair(
        TemperatureMeasurementJson::minAirTemperatureCentigrade,
        MeasurementsTable.minAirTemperatureCentigrade,
        ResultSet::getBigDecimalByName
    ),
    PropertyColumnPair(
        TemperatureMeasurementJson::avgAirTemperatureCentigrade,
        MeasurementsTable.avgAirTemperatureCentigrade, ResultSet::getBigDecimalByName

    ),
    PropertyColumnPair(
        TemperatureMeasurementJson::maxAirTemperatureCentigrade,
        MeasurementsTable.maxAirTemperatureCentigrade, ResultSet::getBigDecimalByName

    )
)

fun <T> Table.array(name: String, columnType: ColumnType): Column<Array<T>> =
    registerColumn(name, ArrayColumnType(columnType))

class ArrayColumnType(
    private val type: ColumnType
) : ColumnType() {

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
                return splitAndTrimTokensToArrayWithLength24(value) { BigDecimal(it) }
            } else if (type is IntegerColumnType) {
                return splitAndTrimTokensToArrayWithLength24(value) { it.toInt() }
            }
        }
        error("Unexpected array component type")
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


private fun ResultSet.getBigDecimalArray24(columnName: String): Array<BigDecimal?> {
    val value = getString(columnName)
    return splitAndTrimTokensToArrayWithLength24(value) { BigDecimal(it) }
}

private fun ResultSet.getIntArray24(columnName: String): Array<Int?> {
    val value = getString(columnName)
    return splitAndTrimTokensToArrayWithLength24(value) { it.toInt() }
}