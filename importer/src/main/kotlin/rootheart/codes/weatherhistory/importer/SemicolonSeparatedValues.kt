package rootheart.codes.weatherhistory.importer

import java.io.ByteArrayInputStream
import java.util.stream.Collectors

data class SemicolonSeparatedValues(
    val columnNames: List<String>,
    val rows: List<List<String?>>
)
