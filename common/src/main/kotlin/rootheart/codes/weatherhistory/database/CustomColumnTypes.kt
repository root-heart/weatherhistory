package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.Table
import rootheart.codes.common.strings.splitAndTrimTokensToArrayWithLength24
import java.math.BigDecimal


fun <T> Table.array(name: String, columnType: ColumnType): Column<Array<T>> =
    registerColumn(name, ArrayColumnType(columnType))

fun Table.decimalArray(name: String): Column<Array<BigDecimal?>> =
    registerColumn(name, DecimalArrayColumnType())

fun Table.intArray(name: String): Column<Array<Int?>> =
    registerColumn(name, IntArrayColumnType())

class DecimalArrayColumnType : ColumnType() {
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
            return splitAndTrimTokensToArrayWithLength24(value, ::BigDecimal)
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
class IntArrayColumnType : ColumnType() {
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
            return splitAndTrimTokensToArrayWithLength24(value, String::toInt)
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
                return splitAndTrimTokensToArrayWithLength24(value, ::BigDecimal)
            } else if (type is IntegerColumnType) {
                return splitAndTrimTokensToArrayWithLength24(value, String::toInt)
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