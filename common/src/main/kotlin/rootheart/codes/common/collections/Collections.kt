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

////////////////////////////////////////////////////////////////

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

////////////////////////////////////////////////////////////////

fun <E> Collection<E>.nullsafeAvgDecimal(selector: (E) -> BigDecimal?): BigDecimal? =
    this.nullsafeSumDecimals(selector)?.let { it / BigDecimal(size) }

fun <E> Collection<E>.nullsafeAvgInt(selector: (E) -> Int?): Int? =
    this.nullsafeSumInts(selector)?.let { it / size }

fun Array<BigDecimal?>.nullsafeAvgDecimals(): BigDecimal? =
    nullsafeSum(iterator(), BigDecimal::plus) { it }?.let { it / BigDecimal(size) }

fun Array<Int?>.nullsafeAvgInts(): Int? =
    nullsafeSum(iterator(), Int::plus) { it }?.let { it / size }


////////////////////////////////////////////////////////////////

fun <E> Iterable<E>.nullsafeSumDecimals(selector: (E) -> BigDecimal?) =
    nullsafeSumDecimals(iterator(), selector)

fun <E> Array<E>.nullsafeSumDecimals(selector: (E) -> BigDecimal?) =
    nullsafeSum(iterator(), BigDecimal::plus, selector)

fun <E> nullsafeSumDecimals(iterator: Iterator<E>, selector: (E) -> BigDecimal?) =
    nullsafeSum(iterator, BigDecimal::plus, selector)

inline fun <T> Iterable<T>.nullsafeSumInts(selector: (T) -> Int?): Int? {
    return nullsafeSum(iterator(), Int::plus, selector)
}

fun <E> Array<E>.nullsafeSumInts(selector: (E) -> Int?) =
    nullsafeSum(iterator(), Int::plus, selector)

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
