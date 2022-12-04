package rootheart.codes.common.collections

import rootheart.codes.weatherhistory.database.MeasurementsTable
import java.math.BigDecimal
import java.math.RoundingMode

inline fun <T, N : Comparable<N>> Iterable<T>.nullsafeMin(selector: (T) -> N?): N? {
    return nullsafeMin(iterator(), selector)
}

inline fun <T, N : Comparable<N>> Array<T>.nullsafeMin(selector: (T) -> N?): N? {
    return nullsafeMin(iterator(), selector)
}

inline fun <N : Comparable<N>> Array<N?>.nullsafeMin(): N? {
    return nullsafeMin(iterator()) { it }
}

inline fun <T, N : Comparable<N>> nullsafeMin(iterator: Iterator<T>, selector: (T) -> N?): N? {
    if (!iterator.hasNext()) return null
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

inline fun <T, N : Comparable<N>> Iterable<T>.nullsafeMax(selector: (T) -> N?): N? {
    return nullsafeMax(iterator(), selector)
}

inline fun <T, N : Comparable<N>> Array<T>.nullsafeMax(selector: (T) -> N?): N? {
    return nullsafeMax(iterator(), selector)
}

inline fun <N : Comparable<N>> Array<N?>.nullsafeMax(): N? {
    return nullsafeMax(iterator()) { it }
}

inline fun <T, N : Comparable<N>> nullsafeMax(iterator: Iterator<T>, selector: (T) -> N?): N? {
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


inline fun <T> Iterable<T>.nullsafeAvg(selector: (T) -> BigDecimal?): BigDecimal? {
    val countNonNull = count { selector(it) != null }.toLong()
    return nullsafeSum(iterator(), BigDecimal::plus, selector)?.divide(
        BigDecimal.valueOf(countNonNull),
        RoundingMode.HALF_UP
    )
}

inline fun Array<BigDecimal?>.nullsafeAvg(): BigDecimal? {
    val countNonNull = count { it != null }.toLong()
    return nullsafeSum(iterator(), BigDecimal::plus) { it }?.divide(
        BigDecimal.valueOf(countNonNull),
        RoundingMode.HALF_UP
    )
}

inline fun Array<Int?>.nullsafeAvg(): Int? {
    val countNonNull = count { it != null }
    return nullsafeSum(iterator(), Int::plus) { it }?.div(countNonNull)
}

inline fun <T> Iterable<T>.nullsafeAvg(selector: (T) -> Int?): Int? {
    val countNonNull = count { selector(it) != null }
    return nullsafeSum(iterator(), Int::plus, selector)?.div(countNonNull)
}

inline fun <T> Iterable<T>.nullsafeSum(selector: (T) -> BigDecimal?): BigDecimal? {
    return nullsafeSum(iterator(), BigDecimal::plus, selector)
}

inline fun <T, reified N : Number> nullsafeSum(
    iterator: Iterator<T>,
    plusFunction: (N, N) -> N,
    selector: (T) -> N?
): N? {
    if (!iterator.hasNext()) return null
    var sum = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (sum == null) {
            sum = v
        } else if (v != null) {
            sum = plusFunction(sum, v)
        }
    }
    return sum
}

fun List<Int?>.generateHistogram(): Map<Int, Int> {
    val histogram = HashMap<Int, Int>()
    filterNotNull()
        .forEach {
        val currentValue = histogram.getOrPut(it) { 0 }
        histogram[it] = currentValue + 1
    }
    return histogram
}