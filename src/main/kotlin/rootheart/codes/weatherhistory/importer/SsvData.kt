package rootheart.codes.weatherhistory.importer

import java.util.stream.Stream

data class SsvData(
    val columnNames: List<String>,
    val columnValuesStream: Stream<List<String>>
)
