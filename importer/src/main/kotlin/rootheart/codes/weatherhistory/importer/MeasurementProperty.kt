package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.PrecipitationType
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty1

open class MeasurementProperty<T>(
    private val property: KMutableProperty1<HourlyMeasurement, T>,
    private val parseValue: (String) -> T
) {
    fun setValue(record: HourlyMeasurement, value: String) = property.set(record, parseValue(value))
}

private val intObjectPool: MutableMap<String, Int> = ConcurrentHashMap()

class IntProperty(property: KMutableProperty1<HourlyMeasurement, Int?>) :
    MeasurementProperty<Int?>(property, { intObjectPool.computeIfAbsent(it, Integer::parseInt) })

private val bigDecimalObjectPool: MutableMap<String, BigDecimal> = ConcurrentHashMap()

class BigDecimalProperty(property: KMutableProperty1<HourlyMeasurement, BigDecimal?>) :
    MeasurementProperty<BigDecimal?>(property, { bigDecimalObjectPool.computeIfAbsent(it, ::BigDecimal) })

class PrecipitationTypeProperty(property: KMutableProperty1<HourlyMeasurement, PrecipitationType?>) :
    MeasurementProperty<PrecipitationType?>(property, PrecipitationType::of)
