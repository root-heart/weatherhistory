package rootheart.codes.weatherhistory.importer

trait SpecUtils {
    List<BigDecimal> allBigDecimalsOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? new BigDecimal(it[index]) : null }
    }
}