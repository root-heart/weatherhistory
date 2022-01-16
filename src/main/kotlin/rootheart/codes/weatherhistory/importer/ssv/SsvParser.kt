package rootheart.codes.weatherhistory.importer.ssv

import java.io.BufferedReader

object SsvParser {
    fun parse(reader: BufferedReader): SsvData {
        val header = reader.readLine() ?: ""
        val columnNames = splitAndTrimTokens(header).map { it!! }
        val columnValues = reader.lines().map { splitAndTrimTokens(it) }
        return SsvData(columnNames, columnValues)
    }

    private fun nullifyValues(values: List<String>) = values.map { nullify(it) }

    private fun nullify(value: String) = if (value == "-999") null else value

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