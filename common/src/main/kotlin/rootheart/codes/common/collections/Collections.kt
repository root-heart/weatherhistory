package rootheart.codes.common.collections

import java.math.BigDecimal
import java.math.RoundingMode

inline fun <T> Iterable<T>.minDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var minValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (minValue == null) {
            minValue = v
        } else if (v != null && minValue > v) {
            minValue = v
        }
    }
    return minValue
}

inline fun <T> Iterable<T>.minInt(selector: (T) -> Int?): Int? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var minValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (minValue == null) {
            minValue = v
        } else if (v != null && minValue > v) {
            minValue = v
        }
    }
    return minValue
}

inline fun <T> Iterable<T>.maxDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (maxValue == null) {
            maxValue = v
        } else if (v != null && maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

inline fun <T> Iterable<T>.maxInt(selector: (T) -> Int?): Int? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (maxValue == null) {
            maxValue = v
        } else if (v != null && maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

inline fun <T> Collection<T>.avgDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val countNonNull = count { selector(it) != null }.toLong()
    return sumDecimal(selector)?.divide(BigDecimal.valueOf(countNonNull), RoundingMode.HALF_UP)
}

inline fun <T> Iterable<T>.avgInt(selector: (T) -> Int?): Int? {
    val countNonNull = count { selector(it) != null }
    return sumInt(selector)?.div(countNonNull)
}

inline fun <T> Collection<T>.sumDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var sum = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        v?.let { sum = sum?.add(it) ?: it }
    }
    return sum
}

inline fun <T> Iterable<T>.sumInt(selector: (T) -> Int?): Int? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var sum = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (v != null) {
            if (sum == null) {
                sum = 0
            }
            sum += v
        }
    }
    return sum
}

//inline fun <T> Collection<T>.minDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
//    val iterator = iterator()
//    if (!iterator.hasNext()) return null
//    var min = selector(iterator.next())
//    while (iterator.hasNext()) {
//        val v = selector(iterator.next())
//        v?.let { min = if (it < min) it else min }
//    }
//    return min
//}
