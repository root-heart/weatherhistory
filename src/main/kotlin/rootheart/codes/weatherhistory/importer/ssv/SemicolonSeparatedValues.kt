package rootheart.codes.weatherhistory.importer.ssv

import java.io.BufferedReader
import java.util.stream.Collectors

data class SemicolonSeparatedValues(
    val columnNames: List<String>,
    val rows: List<List<String?>>
)

object SemicolonSeparatedValuesParser {
    fun parse(reader: BufferedReader): SemicolonSeparatedValues {
        val header = reader.readLine() ?: ""
        val columnNames = splitAndTrimTokens(header).map { it!! }
        val columnValues = reader.lines().map { splitAndTrimTokens(it) }.collect(Collectors.toList())
        return SemicolonSeparatedValues(columnNames, columnValues)
    }

    private fun splitAndTrimTokens(line: String, list: MutableList<String?> = ArrayList()): List<String?> {
        var pos = 0
        var end = line.indexOf(';', pos)
        while (end >= 0) {
            while (line[pos] == ' ') {
                pos++
            }
            while (line[end - 1] == ' ') {
                end--
            }
            val column: String = line.substring(pos, end)
            if (column.endsWith("-999")) {
                list.add(null)
            } else {
                list.add(column)
            }
            pos = end + 1
            end = line.indexOf(';', pos)
        }
        return list
    }
}