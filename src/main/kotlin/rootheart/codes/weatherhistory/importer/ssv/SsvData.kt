package rootheart.codes.weatherhistory.importer.ssv

import java.util.stream.Stream

data class SsvData(
    val columnNames: List<String>,
    val rows: List<List<String?>>
)
