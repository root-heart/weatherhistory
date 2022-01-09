package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.*
import rootheart.codes.weatherhistory.model.CloudType
import rootheart.codes.weatherhistory.model.MeasurementOrObservation
import rootheart.codes.weatherhistory.model.PrecipitationType
import rootheart.codes.weatherhistory.model.QualityLevel
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty1


fun interface RecordProperty<R> {
    fun setValue(record: R, value: String)
}

open class SimpleRecordProperty<R, T>(
    private val property: KMutableProperty1<R, T>,
    private val parseValue: (String) -> T
) : RecordProperty<R> {
    override fun setValue(record: R, value: String) = property.set(record, parseValue(value))
}

open class NestedRecordProperty<R, P1, P2>(
    private val firstProperty: KMutableProperty1<R, P1?>,
    private val constructFirstPropertyValue: () -> P1,
    private val secondProperty: KMutableProperty1<P1, P2>,
    private val parseValue: (String) -> P2
) : RecordProperty<R> {
    override fun setValue(record: R, value: String) {
        var firstPropertyValue = firstProperty.get(record)
        if (firstPropertyValue == null) {
            firstPropertyValue = constructFirstPropertyValue()
            firstProperty.set(record, firstPropertyValue)
        }
        secondProperty.set(firstPropertyValue!!, parseValue(value))
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

class PrecipitationTypeProperty<R>(property: KMutableProperty1<R, PrecipitationType?>) :
    SimpleRecordProperty<R, PrecipitationType?>(property, PrecipitationType::of)
