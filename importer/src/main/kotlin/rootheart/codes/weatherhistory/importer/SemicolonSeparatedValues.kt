package rootheart.codes.weatherhistory.importer

class SemicolonSeparatedValues(val columnNames: List<String>, rows: List<List<String?>>) {
    val rows: List<Row>

    init {
        this.rows = rows.map { Row(it) }
    }

    inner class Row(private val columnValues: List<String?>) {
        operator fun get(columnName: String) = columnValues[columnNames.indexOf(columnName)]
        operator fun get(columnIndex: Int) = columnValues[columnIndex]
    }
}