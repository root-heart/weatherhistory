package rootheart.codes.weatherhistory.importer.converter

import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.importer.ssv.SsvData
import rootheart.codes.weatherhistory.model.StationId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

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

    fun convert(ssvData: SsvData): Stream<R> {
//        validateColumnNames(ssvData)
//        determineIndicesOfColumnsAlwaysPresent(ssvData)
//        return convertValues(ssvData)
        return Stream.empty()
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

    fun determineIndicesOfColumnsAlwaysPresent(columnNames: List<String>) {
        columnIndexStationId = columnNames.indexOf(COLUMN_NAME_STATION_ID)
        columnIndexMeasurementTime = columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
    }

//    private fun convertValues(ssvData: SsvData): Stream<R> {
//        return ssvData.columnValuesStream
//            .map { createRecord(ssvData.columnNames, it) }
//            .filter { it != null }
//            .map { it!! }
//    }

     fun createRecord(columnNames: List<String>, values: List<String?>): R? {
        val stationIdString = values[columnIndexStationId]
        val measurementTimeString = values[columnIndexMeasurementTime]
        if (stationIdString == null || measurementTimeString == null) {
            return null
        }
        val record = recordConstructor.invoke()
        record.stationId = StationId.of(stationIdString.trim())
        record.measurementTime = LocalDateTime.parse(measurementTimeString.trim(), DATE_TIME_FORMATTER)
        for (i in columnNames.indices) {
            if (i == columnIndexMeasurementTime || i == columnIndexStationId) {
                continue
            }
            val columnName = columnNames[i]
            if (columnName == "eor") {
                continue
            }
            val stringValue = values[i]
            if (stringValue != null) {
                val recordProperty = columnMappings.getValue(columnName)
                recordProperty.setValue(record, stringValue.trim())
            }
        }
        return record
    }
}

class InvalidColumnsException(message: String) : Exception(message)