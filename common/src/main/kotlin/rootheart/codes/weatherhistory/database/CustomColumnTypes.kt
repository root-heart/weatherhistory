package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import rootheart.codes.common.strings.splitAndTrimTokensToList
import java.math.BigDecimal

fun Table.decimalArray(name: String): Column<List<BigDecimal?>> =
    registerColumn(name, DecimalArrayColumnType())

fun Table.intArray(name: String): Column<List<Int?>> =
    registerColumn(name, IntArrayColumnType())

class DecimalArrayColumnType : ColumnType() {
    override fun sqlType(): String = "TEXT"

    override fun valueToDB(value: Any?): Any? {
        return if (value is List<*>) {
            value.joinToString(",")
        } else {
            super.valueToDB(value)
        }
    }

    override fun valueFromDB(value: Any): Any {
        if (value is String) {
            return splitAndTrimTokensToList(value, ::BigDecimal)
        }
        error("Unexpected array component type")
    }

    override fun notNullValueToDB(value: Any): Any {
        if (value is List<*>) {
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
    override fun sqlType(): String = "TEXT"

    override fun valueToDB(value: Any?): Any? {
        return if (value is List<*>) {
            value.joinToString(",")
        } else {
            super.valueToDB(value)
        }
    }

    override fun valueFromDB(value: Any): Any {
        if (value is String) {
            return splitAndTrimTokensToList(value, String::toInt)
        }
        error("Unexpected array component type")
    }

    override fun notNullValueToDB(value: Any): Any {
        if (value is List<*>) {
            if (value.isEmpty()) {
                return ""
            }
            return value.joinToString(",")
        } else {
            return super.notNullValueToDB(value)
        }
    }
}

//class ArrayColumnType(
//    private val type: ColumnType
//) : ColumnType() {
//
//    override fun sqlType(): String = "VARCHAR(200)"
//
//    override fun valueToDB(value: Any?): Any? {
//        return if (value is Array<*>) {
//            value.joinToString(",")
//        } else {
//            super.valueToDB(value)
//        }
//    }
//
//    override fun valueFromDB(value: Any): Any {
//        if (value is String) {
//            if (type is DecimalColumnType) {
//                return splitAndTrimTokensToArrayWithLength24(value, ::BigDecimal)
//            } else if (type is IntegerColumnType) {
//                return splitAndTrimTokensToArrayWithLength24(value, String::toInt)
//            }
//        }
//        error("Unexpected array component type")
//    }
//
//    override fun notNullValueToDB(value: Any): Any {
//        if (value is Array<*>) {
//            if (value.isEmpty()) {
//                return ""
//            }
//            return value.joinToString(",")
//        } else {
//            return super.notNullValueToDB(value)
//        }
//    }
//}