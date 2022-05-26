package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.PrecipitationType
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty1


fun interface MeasurementProperty<R> {
    fun setValue(record: R, value: String)
}

open class SimpleMeasurementProperty< T>(
    private val property: KMutableProperty1<HourlyMeasurement, T>,
    private val parseValue: (String) -> T
) : MeasurementProperty<HourlyMeasurement> {
    override fun setValue(record: HourlyMeasurement, value: String) = property.set(record, parseValue(value))
}

private val intObjectPool: MutableMap<String, Int> = ConcurrentHashMap()

class IntProperty(property: KMutableProperty1<HourlyMeasurement, Int?>) :
    SimpleMeasurementProperty<Int?>(property, { intObjectPool.computeIfAbsent(it, Integer::parseInt) })

private val bigDecimalObjectPool: MutableMap<String, BigDecimal> = ConcurrentHashMap()

class BigDecimalProperty(property: KMutableProperty1<HourlyMeasurement, BigDecimal?>) :
    SimpleMeasurementProperty<BigDecimal?>(property, { bigDecimalObjectPool.computeIfAbsent(it, ::BigDecimal) })

class PrecipitationTypeProperty(property: KMutableProperty1<HourlyMeasurement, PrecipitationType?>) :
    SimpleMeasurementProperty<PrecipitationType?>(property, PrecipitationType::of)
