package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.importer.ssv.SsvData
import rootheart.codes.weatherhistory.model.StationId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

private const val NULL_STRING = "-999"

open class RecordConverter<R : BaseRecord>(
    private val recordConstructor: () -> R,
    private val columnMappings: Map<String, RecordProperty<R>>,
    private var columnIndexStationId: Int = 0,
    private var columnIndexMeasurementTime: Int = 0,
) {
    companion object {
        @JvmStatic
        private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")
        private const val COLUMN_NAME_STATION_ID = "STATIONS_ID"
        private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"
    }

    fun validateColumnNames(columnNames: List<String>) {
        val expectedColumnNames = columnMappings.keys + COLUMN_NAME_STATION_ID + COLUMN_NAME_MEASUREMENT_TIME
        columnNames.forEach {
            if (!expectedColumnNames.contains(it) && it != "eor") {
                throw InvalidColumnsException("columnName $it not expected")
            }
        }
        expectedColumnNames.forEach {
            if (!columnNames.contains(it)) {
                throw InvalidColumnsException("column name $it expected, but not found")
            }
        }
    }

    private var recordProperties = Array<RecordProperty<R>?>(0) { null }


    fun determineIndicesOfColumnsAlwaysPresent(columnNames: List<String>) {
        columnIndexStationId = columnNames.indexOf(COLUMN_NAME_STATION_ID)
        columnIndexMeasurementTime = columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        recordProperties = Array(columnNames.size) { index ->
            if (index != columnIndexStationId && index != columnIndexMeasurementTime) {
                columnMappings.getValue(columnNames[index])
            } else {
                null
            }
        }
    }

    fun createRecord(values: List<String>): R {
        val stationIdString = values[columnIndexStationId]
        val measurementTimeString = values[columnIndexMeasurementTime]
        val record = recordConstructor()
        record.stationId = StationId.of(stationIdString)
        record.measurementTime = LocalDateTime.parse(measurementTimeString, DATE_TIME_FORMATTER)
        for (i in values.indices) {
            if (i == columnIndexMeasurementTime || i == columnIndexStationId) {
                continue
            }
            val value = values[i]
            if (value == "eor") {
                continue
            }
            val stringValue = values[i]
            if (stringValue != NULL_STRING) {
                val recordProperty = recordProperties[i]!!//columnMappings.getValue(columnName)
                recordProperty.setValue(record, stringValue)
            }
        }
        return record
    }
}

class InvalidColumnsException(message: String) : Exception(message)