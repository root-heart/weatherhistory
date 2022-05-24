package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.database.PrecipitationType
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty1


fun interface MeasurementProperty<R> {
    fun setValue(record: R, value: String)
}

open class SimpleMeasurementProperty<R, T>(
    private val property: KMutableProperty1<R, T>,
    private val parseValue: (String) -> T
) : MeasurementProperty<R> {
    override fun setValue(record: R, value: String) = property.set(record, parseValue(value))
}

private val intObjectPool: MutableMap<String, Int> = ConcurrentHashMap()

class IntProperty<R>(property: KMutableProperty1<R, Int?>) :
    SimpleMeasurementProperty<R, Int?>(property, { intObjectPool.computeIfAbsent(it, Integer::parseInt) })

private val bigDecimalObjectPool: MutableMap<String, BigDecimal> = ConcurrentHashMap()

class BigDecimalProperty<R>(property: KMutableProperty1<R, BigDecimal?>) :
    SimpleMeasurementProperty<R, BigDecimal?>(property, { bigDecimalObjectPool.computeIfAbsent(it, ::BigDecimal) })

class PrecipitationTypeProperty<R>(property: KMutableProperty1<R, PrecipitationType?>) :
    SimpleMeasurementProperty<R, PrecipitationType?>(property, PrecipitationType::of)
