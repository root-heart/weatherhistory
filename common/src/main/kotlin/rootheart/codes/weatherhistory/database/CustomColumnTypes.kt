package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import rootheart.codes.common.strings.splitAndTrimTokensToList
import java.math.BigDecimal

fun Table.decimalArray(name: String): Column<Array<BigDecimal?>> =
        registerColumn(name, DecimalArrayColumnType())

fun Table.intArray(name: String): Column<Array<Int>> =
        registerColumn(name, IntArrayColumnType())

fun Table.intArrayNullable(name: String): Column<Array<Int?>?> =
        registerColumn(name, IntArrayColumnType())

fun Table.decimalArrayNullable(name: String): Column<Array<BigDecimal?>?> =
        registerColumn(name, DecimalArrayColumnType())

fun Table.generatedDateColumn(name: String, definition: String): Column<DateTime> =
        registerColumn(name, object : ColumnType(true) {
            override fun sqlType(): String {
                return "TIMESTAMP GENERATED ALWAYS AS ($definition) STORED"
            }

            override fun valueFromDB(value: Any): Any = when(value) {
                is DateTime -> value
                is java.sql.Date ->  DateTime(value.time)
                is java.sql.Timestamp -> DateTime(value.time)
                else -> valueFromDB(value.toString())
            }
        })

class DecimalArrayColumnType : ColumnType(true) {
    override fun sqlType(): String = "TEXT"

    override fun valueToDB(value: Any?) = if (value is Array<*>) {
        value.joinToString(",")
    } else {
        super.valueToDB(value)
    }

    override fun valueFromDB(value: Any): Any {
        if (value is String) {
            return splitAndTrimTokensToList(value) { if (it == "") null else BigDecimal(it) }.toTypedArray()
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

class IntArrayColumnType() : ColumnType(true) {
    override fun sqlType(): String = "TEXT"

    override fun valueToDB(value: Any?): Any? {
        return if (value is Array<*>) {
            value.joinToString(",")
        } else {
            super.valueToDB(value)
        }
    }

    override fun valueFromDB(value: Any): Any {
        if (value is String) {
            return splitAndTrimTokensToList(value) { if (it == "") null else it.toInt() }.toTypedArray()
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
