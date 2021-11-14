package rootheart.codes.weatherhistory.importer

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
}