package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.SsvData
import rootheart.codes.weatherhistory.importer.StationId
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

open class RecordConverter<R : BaseRecord>(
    private val recordConstructor: () -> R,
    private val columnMappings: Map<String, RecordProperty<R, *>>,
    private var columnIndexStationId: Int = 0,
    private var columnIndexMeasurementTime: Int = 0,
) {
    companion object {
        @JvmStatic
        private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")
        private const val COLUMN_NAME_STATION_ID = "STATIONS_ID"
        private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"
    }

    fun convert(ssvData: SsvData): Stream<R> {
        validateColumnNames(ssvData)
        determineIndicesOfColumnsAlwaysPresent(ssvData)
        return convertValues(ssvData)
    }

    private fun validateColumnNames(ssvData: SsvData) {
        val expectedColumnNames = columnMappings.keys + COLUMN_NAME_STATION_ID + COLUMN_NAME_MEASUREMENT_TIME
        ssvData.columnNames.forEach {
            if (!expectedColumnNames.contains(it)) {
                throw InvalidColumnsException("columnName $it not expected")
            }
        }
        expectedColumnNames.forEach {
            if (!ssvData.columnNames.contains(it)) {
                throw InvalidColumnsException("column name $it expected, but not found")
            }
        }
    }

    private fun determineIndicesOfColumnsAlwaysPresent(ssvData: SsvData) {
        columnIndexStationId = ssvData.columnNames.indexOf(COLUMN_NAME_STATION_ID)
        columnIndexMeasurementTime = ssvData.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
    }

    private fun convertValues(ssvData: SsvData): Stream<R> {
        return ssvData.columnValuesStream.map { createRecord(ssvData.columnNames, it) }
    }

    private fun createRecord(columnNames: List<String>, values: List<String>): R {
        val record = recordConstructor.invoke()
        record.stationId = StationId.of(values[columnIndexStationId])
        record.measurementTime = LocalDateTime.parse(values[columnIndexMeasurementTime], DATE_TIME_FORMATTER)
        for (i in columnNames.indices) {
            if (i == columnIndexMeasurementTime || i == columnIndexStationId) {
                continue
            }
            val columnName = columnNames[i]
            val stringValue = values[i]
            if (stringValue != "-999" && stringValue != "-99.9") {
                val recordProperty = columnMappings.getValue(columnName)
                recordProperty.setValue(record, stringValue)
            }
        }
        return record
    }
}

class InvalidColumnsException(message: String) : Exception(message)