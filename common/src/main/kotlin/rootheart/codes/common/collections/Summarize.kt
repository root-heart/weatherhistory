package rootheart.codes.common.collections

import org.joda.time.LocalDate
import java.math.BigDecimal

class AvgMaxDetails<N : Number>(
        var avg: N? = null,
        var max: N? = null,
        var maxDay: LocalDate? = null,
        var details: Array<N?>? = null)

class MinAvgMaxDetails<N : Number>(
        var min: N? = null,
        var minDay: LocalDate? = null,
        var avg: N? = null,
        var max: N? = null,
        var maxDay: LocalDate? = null,
        var details: Array<N?>? = null)

class MinMaxSumDetails<N : Number>(
        var min: N? = null,
        var minDay: LocalDate? = null,
        var max: N? = null,
        var maxDay: LocalDate? = null,
        var sum: N? = null,
        var details: Array<N?>? = null)

class Histogram(var histogram: Array<Int>? = null, var details: Array<Int?>? = null)

inline fun <reified T> List<T>.minAvgMaxDecimals(selector: (T) -> MinAvgMaxDetails<BigDecimal>): MinAvgMaxDetails<BigDecimal> {
    val notNullValues = mapNotNull { selector(it) }
    val minMeasurement = notNullValues.filter { it.min != null }.minByOrNull { it.min!! }
    val maxMeasurement = notNullValues.filter { it.max != null }.maxByOrNull { it.max!! }
    val details = notNullValues.map { it.avg }.toTypedArray()
    return MinAvgMaxDetails(
            min = minMeasurement?.min,
            minDay = minMeasurement?.minDay,
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.max,
            maxDay = maxMeasurement?.maxDay,
            details = details
    )
}

fun <T> List<T>.avgMaxDecimals(selector: (T) -> AvgMaxDetails<BigDecimal>): AvgMaxDetails<BigDecimal> {
    val notNullValues = mapNotNull { selector(it) }
    val maxMeasurement = notNullValues.filter { it.max != null }.maxByOrNull { it.max!! }
    val details = notNullValues.map { it.avg }.toTypedArray()
    return AvgMaxDetails(
            avg = nullsafeAvgDecimal { selector(it).avg },
            max = maxMeasurement?.max,
            maxDay = maxMeasurement?.maxDay,
            details = details
    )
}

fun <T> List<T>.minAvgMaxInts(selector: (T) -> MinAvgMaxDetails<Int>): MinAvgMaxDetails<Int> {
    val notNullValues = mapNotNull { selector(it) }
    val minMeasurement = notNullValues.filter { it.min != null }.minByOrNull { it.min!! }
    val maxMeasurement = notNullValues.filter { it.max != null }.maxByOrNull { it.max!! }
    val details = notNullValues.map { it.avg }.toTypedArray()
    return MinAvgMaxDetails(
            min = minMeasurement?.min,
            minDay = minMeasurement?.minDay,
            avg = nullsafeAvgInt { selector(it).avg },
            max = maxMeasurement?.max,
            maxDay = maxMeasurement?.maxDay,
            details = details
    )
}

fun <T> List<T>.minMaxSumDecimals(selector: (T) -> MinMaxSumDetails<BigDecimal>): MinMaxSumDetails<BigDecimal> {
    val notNullValues = mapNotNull { selector(it) }
    val minMeasurement = notNullValues.filter { it.min != null }.minByOrNull { it.min!! }
    val maxMeasurement = notNullValues.filter { it.max != null }.maxByOrNull { it.max!! }
    val details = notNullValues.map { it.sum }.toTypedArray()
    return MinMaxSumDetails(
            min = minMeasurement?.min,
            minDay = minMeasurement?.minDay,
            max = maxMeasurement?.max,
            maxDay = maxMeasurement?.maxDay,
            sum = nullsafeSumDecimals { selector(it).sum },
            details = details
    )
}

fun <T> List<T>.minMaxSumInts(selector: (T) -> MinMaxSumDetails<Int>): MinMaxSumDetails<Int> {
    val notNullValues = mapNotNull { selector(it) }
    val minMeasurement = notNullValues.filter { it.min != null }.minByOrNull { it.min!! }
    val maxMeasurement = notNullValues.filter { it.max != null }.maxByOrNull { it.max!! }
    val details = notNullValues.map { it.sum }.toTypedArray()
    return MinMaxSumDetails(
            min = minMeasurement?.min,
            minDay = minMeasurement?.minDay,
            max = maxMeasurement?.max,
            maxDay = maxMeasurement?.maxDay,
            sum = nullsafeSumInts { selector(it).sum },
            details = details
    )
}
