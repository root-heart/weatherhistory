package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.model.MeasurementOrObservation
import rootheart.codes.weatherhistory.model.PrecipitationType
import rootheart.codes.weatherhistory.model.QualityLevel

trait SpecUtils {
    List<BigDecimal> allBigDecimalsOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? new BigDecimal(it[index]) : null }
    }

    List<Integer> allIntsOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? Integer.parseInt(it[index]) : null }
    }

    List<QualityLevel> allQualityLevelsOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? QualityLevel.of(it[index]) : null }
    }

    List<PrecipitationType> allPrecipitationTypesOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? PrecipitationType.of(it[index]) : null }
    }

    List<MeasurementOrObservation> allMeasurementOrObservationsOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? MeasurementOrObservation.of(it[index]) : null }
    }
}