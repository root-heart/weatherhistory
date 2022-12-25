package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import rootheart.codes.common.strings.splitAndTrimTokensToList
import java.math.BigDecimal

fun Table.decimalArray(name: String): Column<Array<BigDecimal?>> =
    registerColumn(name, DecimalArrayColumnType())

fun Table.intArray(name: String): Column<Array<Int>> =
    registerColumn(name, IntArrayColumnType())

fun Table.intArrayNullable(name: String): Column<Array<Int?>> =
    registerColumn(name, IntArrayColumnType())

class DecimalArrayColumnType : ColumnType() {
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

class IntArrayColumnType(nullable: Boolean = true) : ColumnType() {
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
