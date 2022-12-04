package rootheart.codes.weatherhistory.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.common.strings.splitAndTrimTokensToArrayWithLength24
import java.math.BigDecimal
import java.sql.Date
import java.sql.ResultSet
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0

private val log = KotlinLogging.logger {}

abstract class PropertyColumnBinding<O, T, R>(
    val property: KMutableProperty1<O, in T>,
    val column: Column<out T>
) {
    abstract fun setValueFromResultInProperty(r: R, o: O)
}

class JdbcPropertyColumnBinding<O, T>(
    property: KMutableProperty1<O, in T>,
    column: Column<out T>,
    val resultSetGetter: KFunction2<ResultSet, String, T>
) : PropertyColumnBinding<O, T, ResultSet>(property, column) {
    override fun setValueFromResultInProperty(r: ResultSet, o: O) = property.set(o, resultSetGetter(r, column.name))
}

class ExposedPropertyColumnBinding<O, D>(
    property: KMutableProperty1<O, in D>,
    column: Column<out D>,
) : PropertyColumnBinding<O, D, ResultRow>(property, column) {
    override fun setValueFromResultInProperty(r: ResultRow, o: O) = property.set(o, r[column])
}

fun <O, D> bind(
    property: KMutableProperty1<O, in D>,
    column: Column<out D>
) = ExposedPropertyColumnBinding(property, column)

fun ResultSet.getBigDecimalByName(columnName: String) = getBigDecimal(columnName)

fun ResultSet.getDateTimeByName(columnName: String) = DateTime(getDate(columnName))

abstract class Dao {
    fun findByYear(station: Station, year: Int): List<Map<String, Any?>> =
        measureAndLogDuration("TemperatureMeasurementDao.findDailyByYear(${station.id}, $year)") {
            val start = LocalDate(year, 1, 1)
            val end = start.plusYears(1)
            return@measureAndLogDuration fetchFromDb(station.id!!, start, end)
        }

    fun findByYearAndMonth(station: Station, year: Int, month: Int): List<Map<String, Any?>> =
        measureAndLogDuration("TemperatureMeasurementDao.findDailyByYearAndMonth(${station.id}, $year, $month)") {
            val start = LocalDate(year, month, 1)
            val end = start.plusMonths(1)
            return@measureAndLogDuration fetchFromDb(station.id!!, start, end)
        }

    fun findByYearMonthAndDay(station: Station, year: Int, month: Int, day: Int): Map<String, Any?>? =
        measureAndLogDuration("TemperatureMeasurementDao.findHourlyByYearMonthAndDay(${station.id}, $year, $month, $day)") {
            val start = LocalDate(year, month, day)
            val end = start
            return@measureAndLogDuration fetchFromDb(station.id!!, start, end).firstOrNull()
        }

    abstract fun fetchFromDb(stationId: Long, start: LocalDate, end: LocalDate): List<Map<String, Any?>>
}

// TODO JdbcDao seems to be much faster than ExposedDao.
// It results in a 50% faster response time for a whole GET request with the db access being almost twice as fast.
// Keep it for possible future usage
abstract class JdbcDao(vararg columnsToSelect: KProperty0<Column<*>>) : Dao() {
    private val sql: String
    private val columns: List<KProperty0<Column<*>>>

    init {
        columns = ArrayList()
        columns.addAll(columnsToSelect)
        columns.add(MeasurementsTable::day)
        sql = "select ${columns.joinToString(",") { it.get().name }} " +
                "from ${MeasurementsTable.tableName} " +
                "where ${MeasurementsTable.stationId.name} = ? " +
                "and ${MeasurementsTable.day.name} between ? and ?"
    }

    override fun fetchFromDb(stationId: Long, start: LocalDate, end: LocalDate): List<Map<String, Any?>> = transaction {
        WeatherDb.dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                log.info { "Executing $sql" }
                stmt.setLong(1, stationId)
                stmt.setDate(2, Date(start.toDate().time))
                stmt.setDate(3, Date(end.toDate().time))
                stmt.executeQuery().use { rs ->
                    measureAndLogDuration("create json($stationId, resultSet)") {
                        val measurements = ArrayList<Map<String, Any?>>()
                        while (rs.next()) {
                            measurements += buildJsonMap(rs)
                        }
                        return@measureAndLogDuration measurements
                    }
                }
            }
        }
    }

    private fun buildJsonMap(rs: ResultSet) = columns.associate {
        val columnType = it.get().columnType
        val columnName = it.get().name
        it.name to when (columnType) {
            is DateColumnType -> LocalDate(rs.getDate(columnName)).toString(DATE_TIME_PATTERN)
            is DecimalArrayColumnType -> rs.getBigDecimalArray24(columnName)
            is DecimalColumnType -> rs.getBigDecimal(columnName)
            is IntArrayColumnType -> rs.getIntArray24(columnName)
            is IntegerColumnType -> rs.getInt(columnName)
            else -> rs.getObject(columnName)
        }
    }
}

data class MonthlyMinAvgMax(
    val year: Int,
    val month: Int,
    val min: Number?,
    val avg: Number?,
    val max: Number?,
)

data class DailyMinAvgMax(
    val date: DateTime,
    val min: Number?,
    val avg: Number?,
    val max: Number?,
)

data class MonthlySum(
    val year: Int,
    val month: Int,
    val sum: Int
)

private fun <T> Any.measureTransaction(identifier: String, statement: Transaction.() -> T): T =
    measureAndLogDuration(identifier) { transaction { statement() } }

open class MonthlyMinAvgMaxDao(
    private val minColumn: Column<out Number?>,
    private val avgColumn: Column<out Number?>,
    private val maxColumn: Column<out Number?>,
) {
    private val fieldSet = MonthlySummaryTable.slice(MonthlySummaryTable.month, minColumn, avgColumn, maxColumn)

    private val sql = "select ${MonthlySummaryTable.month.name}, " +
            "${minColumn.name} as min, " +
            "${avgColumn.name} as avg, " +
            "${maxColumn.name} as max " +
            "from ${MonthlySummaryTable.tableName} " +
            "where ${MonthlySummaryTable.stationId.name} = ? " +
            "and ${MonthlySummaryTable.year.name} = ?"

    fun fetchFromDb(stationId: Long, year: Int): List<MonthlyMinAvgMax> =
        measureTransaction("fetchFromDb($stationId, $year)") { exposed(stationId, year) }

    private fun jdbc(stationId: Long, year: Int): List<MonthlyMinAvgMax> =
        WeatherDb.dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                log.info { "Executing $sql" }
                stmt.setLong(1, stationId)
                stmt.setInt(2, year)
                stmt.executeQuery().use { rs -> createJson(rs, year) }
            }
        }

    private fun createJson(rs: ResultSet, year: Int) = measureAndLogDuration("createJson") {
        val measurements = ArrayList<MonthlyMinAvgMax>()
        while (rs.next()) {
            measurements += MonthlyMinAvgMax(
                year = year,
                month = rs.getInt(1),
                min = rs.getBigDecimal(2),
                avg = rs.getBigDecimal(3),
                max = rs.getBigDecimal(4)
            )
        }
        return@measureAndLogDuration measurements
    }

    private fun exposed(stationId: Long, year: Int) =
        fieldSet.select { (MonthlySummaryTable.year eq year) and (MonthlySummaryTable.stationId eq stationId) }
            .map {
                MonthlyMinAvgMax(
                    year = year,
                    month = it[MonthlySummaryTable.month],
                    min = it[minColumn],
                    avg = it[avgColumn],
                    max = it[maxColumn]
                )
            }
}

open class DailyMinAvgMaxDao(
    private val minColumn: Column<out Number?>,
    private val avgColumn: Column<out Number?>,
    private val maxColumn: Column<out Number?>,
) {
    fun fetchFromDb(stationId: Long, year: Int): List<DailyMinAvgMax> =
        measureTransaction("fetchFromDb($stationId, $year)") { exposed(stationId, year) }

    private fun exposed(stationId: Long, year: Int): List<DailyMinAvgMax> {
        val start = LocalDate(year, 1, 1).toDateTimeAtStartOfDay()
        val end = start.plusYears(1)
        return MeasurementsTable
            .slice(MeasurementsTable.day, minColumn, avgColumn, maxColumn)
            .select { (MeasurementsTable.day.between(start, end)) and (MeasurementsTable.stationId eq stationId) }
            .map {
                DailyMinAvgMax(
                    date = it[MeasurementsTable.day],
                    min = it[minColumn],
                    avg = it[avgColumn],
                    max = it[maxColumn]
                )
            }
    }
}

abstract class SummaryJdbcDao(vararg columnsToSelect: KProperty0<Column<*>>) {
    private val sql: String
    private val columns: List<KProperty0<Column<*>>>

    init {
        columns = ArrayList()
        columns.addAll(columnsToSelect)
        columns.add(MonthlySummaryTable::year)
        columns.add(MonthlySummaryTable::month)
        sql = "select ${columns.joinToString(",") { it.get().name }} " +
                "from ${MonthlySummaryTable.tableName} " +
                "where ${MonthlySummaryTable.stationId.name} = ? " +
                "and ${MonthlySummaryTable.year.name} = ?"
    }

    fun fetchFromDb(stationId: Long, year: Int): List<Map<String, Any?>> = transaction {
        WeatherDb.dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                log.info { "Executing $sql" }
                stmt.setLong(1, stationId)
                stmt.setInt(2, year)
                stmt.executeQuery().use { rs ->
                    measureAndLogDuration("create json($stationId, resultSet)") {
                        val measurements = ArrayList<Map<String, Any?>>()
                        while (rs.next()) {
                            val jsonMap = buildJsonMap(rs).toMutableMap()
                            val firstDayInMonth =
                                LocalDate(jsonMap["year"].toString().toInt(), jsonMap["month"].toString().toInt(), 1)
                            jsonMap["day"] = firstDayInMonth.toString(DATE_TIME_PATTERN)
                            measurements += jsonMap
                        }
                        return@measureAndLogDuration measurements
                    }
                }
            }
        }
    }

    private fun buildJsonMap(rs: ResultSet) = columns.associate {
        val columnType = it.get().columnType
        val columnName = it.get().name
        it.name to when (columnType) {
            is DateColumnType -> LocalDate(rs.getDate(columnName)).toString(DATE_TIME_PATTERN)
            is DecimalArrayColumnType -> rs.getBigDecimalArray24(columnName)
            is DecimalColumnType -> rs.getBigDecimal(columnName)
            is IntArrayColumnType -> rs.getIntArray24(columnName)
            is IntegerColumnType -> rs.getInt(columnName)
            else -> rs.getObject(columnName)
        }
    }
}

abstract class ExposedDao(vararg columnsToSelect: KProperty0<Column<*>>) : Dao() {
    private val fieldSet: FieldSet
    private val columns: List<KProperty0<Column<*>>>

    init {
        columns = ArrayList()
        columns.addAll(columnsToSelect)
        columns.add(MeasurementsTable::day)
        fieldSet = MeasurementsTable.slice(columns.map { it.get() })
    }

    override fun fetchFromDb(stationId: Long, start: LocalDate, end: LocalDate): List<Map<String, Any?>> = transaction {
        val x = fieldSet
            .select { MeasurementsTable.byStationIdAndDateBetween(stationId, start, end) }
        return@transaction measureAndLogDuration("create json($stationId, resultSet)") {
            x.map(::buildJsonType)
        }
    }

    private fun buildJsonType(resultRow: ResultRow) = columns.associate {
        it.name to when (it) {
            MeasurementsTable::day -> LocalDate(resultRow[it.get()]).toString(DATE_TIME_PATTERN)
            else -> resultRow[it.get()]
        }
    }
}

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

private fun ResultSet.getBigDecimalArray24(columnName: String): Array<BigDecimal?> {
    val value = getString(columnName)
    return splitAndTrimTokensToArrayWithLength24(value) { BigDecimal(it) }
}

private fun ResultSet.getIntArray24(columnName: String): Array<Int?> {
    val value = getString(columnName)
    return splitAndTrimTokensToArrayWithLength24(value) { it.toInt() }
}