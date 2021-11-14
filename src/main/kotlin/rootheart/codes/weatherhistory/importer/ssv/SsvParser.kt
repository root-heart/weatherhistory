package rootheart.codes.weatherhistory.importer.ssv

import java.io.BufferedReader

class SsvParser {
    fun parse(reader: BufferedReader): SsvData {
        val columnNames = reader.readLine()?.split(";") ?: listOf()
        val columnValues = reader.lines().map { nullifyValues(it.split(";")) }
        return SsvData(columnNames, columnValues)
    }

    private fun nullifyValues(values: List<String>) = values.map { nullify(it) }

    private fun nullify(value: String) = if (value == "-999") null else value
}