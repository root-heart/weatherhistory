package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.CloudLayer
import rootheart.codes.weatherhistory.importer.CloudType
import rootheart.codes.weatherhistory.importer.MeasurementOrObservation
import rootheart.codes.weatherhistory.importer.QualityLevel
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty1


abstract class RecordProperty<R, T> {
    abstract fun setValue(record: R, value: String)
}

open class SimpleRecordProperty<R, T>(
    private val property: KMutableProperty1<R, T>,
    private val valueParser: (String) -> T
) : RecordProperty<R, T>() {
    override fun setValue(record: R, value: String) = property.set(record, valueParser.invoke(value))
}

open class NestedRecordProperty<R, P1, P2>(
    private val firstProperty: KMutableProperty1<R, P1?>,
    private val firstPropertyValueConstructor: () -> P1,
    private val secondProperty: KMutableProperty1<P1, P2>,
    private val valueParser: (String) -> P2
) : RecordProperty<R, P2>() {
    override fun setValue(record: R, value: String) {
        var firstPropertyValue = firstProperty.get(record)
        if (firstPropertyValue == null) {
            firstPropertyValue = firstPropertyValueConstructor.invoke()
            firstProperty.set(record, firstPropertyValue)
        }
        secondProperty.set(firstPropertyValue!!, valueParser.invoke(value))
    }
}


private val intObjectPool: MutableMap<String, Int> = ConcurrentHashMap()

class IntProperty<R>(property: KMutableProperty1<R, Int?>) :
    SimpleRecordProperty<R, Int?>(property, { intObjectPool.computeIfAbsent(it, Integer::parseInt) })

private val bigDecimalObjectPool: MutableMap<String, BigDecimal> = ConcurrentHashMap()

class BigDecimalProperty<R>(property: KMutableProperty1<R, BigDecimal?>) :
    SimpleRecordProperty<R, BigDecimal?>(property, { bigDecimalObjectPool.computeIfAbsent(it, ::BigDecimal) })

class QualityLevelProperty<R>(property: KMutableProperty1<R, QualityLevel?>) :
    SimpleRecordProperty<R, QualityLevel?>(property, QualityLevel::of)

class MeasurementOrObservationProperty<R>(property: KMutableProperty1<R, MeasurementOrObservation?>) :
    SimpleRecordProperty<R, MeasurementOrObservation?>(property, MeasurementOrObservation::of)

class CloudLayerCloudTypeProperty<R>(layerProperty: KMutableProperty1<R, CloudLayer?>) :
    NestedRecordProperty<R, CloudLayer, CloudType?>(
        layerProperty,
        ::CloudLayer,
        CloudLayer::cloudType,
        CloudType::of
    )

class CloudLayerCloudTypeAbbreviationProperty<R>(layerProperty: KMutableProperty1<R, CloudLayer?>) :
    NestedRecordProperty<R, CloudLayer, CloudType?>(
        layerProperty,
        ::CloudLayer,
        CloudLayer::cloudType,
        CloudType::ofAbbreviation
    )

class CloudLayerHeightProperty<R>(layerProperty: KMutableProperty1<R, CloudLayer?>) :
    NestedRecordProperty<R, CloudLayer, Int?>(
        layerProperty,
        ::CloudLayer,
        CloudLayer::height,
        { intObjectPool.computeIfAbsent(it, Integer::parseInt) }
    )

class CloudLayerCoverageProperty<R>(layerProperty: KMutableProperty1<R, CloudLayer?>) :
    NestedRecordProperty<R, CloudLayer, Int?>(
        layerProperty,
        ::CloudLayer,
        CloudLayer::coverage,
        { intObjectPool.computeIfAbsent(it, Integer::parseInt) }
    )
