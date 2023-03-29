package rootheart.codes.common.collections

import java.math.BigDecimal

inline fun <T, N> Iterable<T>.nullsafeMin(selector: (T) -> N?): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMin(iterator(), selector)
}

inline fun <T, N> Array<T>.nullsafeMin(selector: (T) -> N?): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMin(iterator(), selector)
}

inline fun <N> Array<N?>.nullsafeMin(): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMin(iterator()) { it }
}

inline fun <N> Iterable<N?>.nullsafeMin(): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMin(iterator()) { it }
}

inline fun <T, N> nullsafeMin(iterator: Iterator<T>, selector: (T) -> N?): N?
        where N : Comparable<N>, N : Number {
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

inline fun <T, N> Iterable<T>.nullsafeMax(selector: (T) -> N?): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMax(iterator(), selector)
}

inline fun <T, N> Array<T>.nullsafeMax(selector: (T) -> N?): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMax(iterator(), selector)
}

inline fun <N> Array<N?>.nullsafeMax(): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMax(iterator()) { it }
}

inline fun <N> Iterable<N?>.nullsafeMax(): N?
        where N : Comparable<N>, N : Number {
    return nullsafeMax(iterator()) { it }
}

inline fun <T, N> nullsafeMax(iterator: Iterator<T>, selector: (T) -> N?): N?
        where N : Comparable<N>, N : Number {
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

inline fun <E> Iterable<E>.nullsafeAvgDecimal(selector: (E) -> BigDecimal?): BigDecimal? =
    this.nullsafeSumDecimals(selector)
            ?.let { sum ->
                val notNullValuesCount = mapNotNull(selector).count()
                sum / BigDecimal(notNullValuesCount)
            }

fun <E> Collection<E>.nullsafeAvgInt(selector: (E) -> Int?): Int? =
    this.nullsafeSumInts(selector)?.let { it / mapNotNull(selector).count() }

fun Array<BigDecimal?>.nullsafeAvgDecimal(): BigDecimal? = this.nullsafeSumDecimals { it }
        ?.let { sum ->
            val notNullValuesCount = mapNotNull { it } .count()
            sum / BigDecimal(notNullValuesCount)
        }
//fun Iterable<BigDecimal?>.nullsafeAvgDecimals(): BigDecimal? = nullsafeAvgDecimals(iterator())

fun nullsafeAvgDecimals(iterator: Iterator<BigDecimal?>): BigDecimal? =
    nullsafeSum(iterator, BigDecimal::plus) { it }
            ?.let { sum ->
                val countNotNull = countNotNull(iterator)
                return if (countNotNull == 0) null else sum / BigDecimal(countNotNull)
            }


fun <T> countNotNull(iterator: Iterator<T>): Int {
    var notNullValuesCount = 0
    while (iterator.hasNext()) {
        if (iterator.next() != null) {
            notNullValuesCount++
        }
    }
    return notNullValuesCount
}


fun Array<Int?>.nullsafeAvgInts(): Int? =
    nullsafeSum(iterator(), Int::plus) { it }?.let { it / filterNotNull().count() }

fun Iterable<Int?>.nullsafeAvgInts(): Int? =
    nullsafeSum(iterator(), Int::plus) { it }?.let { it / filterNotNull().count() }


////////////////////////////////////////////////////////////////

inline fun <E> Iterable<E>.nullsafeSumDecimals(selector: (E) -> BigDecimal?) =
    nullsafeSumDecimals(iterator(), selector)

fun <E> Array<E>.nullsafeSumDecimals(selector: (E) -> BigDecimal?) =
    nullsafeSum(iterator(), BigDecimal::plus, selector)

inline fun <E> nullsafeSumDecimals(iterator: Iterator<E>, selector: (E) -> BigDecimal?) =
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
