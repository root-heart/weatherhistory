package rootheart.codes.weatherhistory.importer

import java.io.BufferedReader

class SsvParser {
    fun parse(reader: BufferedReader): SsvData {
        val columnNames = reader.readLine().split(";")
        val columnValues = reader.lines()
            .map { it.split(";") }
        return SsvData(columnNames, columnValues)
    }
}