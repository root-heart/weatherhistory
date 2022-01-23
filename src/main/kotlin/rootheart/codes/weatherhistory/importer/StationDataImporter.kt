package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.importer.html.RecordType
import rootheart.codes.weatherhistory.importer.html.ZippedDataFile
import rootheart.codes.weatherhistory.importer.ssv.SsvData
import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import rootheart.codes.weatherhistory.model.StationId
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.util.zip.ZipInputStream

object StationDataImporter {
    private val log = KotlinLogging.logger {}

    fun import(zippedDataFiles: List<ZippedDataFile>) {
        log.info { "import(${zippedDataFiles.size} zipped data files)" }
        val stationId = getStationId(zippedDataFiles)
        val measurements = Downloader.download(zippedDataFiles)
        val summarizedMeasurements = Summarizer.summarizeMeasurements(stationId, measurements)
        SummarizedMeasurementImporter.importEntities(summarizedMeasurements)
        log.info { "import(${zippedDataFiles.size} zipped data files) finished" }
    }

    private fun getStationId(zippedDataFiles: List<ZippedDataFile>): StationId {
        val stationIds = zippedDataFiles.map { it.stationId }.distinct()
        if (stationIds.size != 1) {
            throw IllegalArgumentException()
        }
        return stationIds.first()
    }
}

object Downloader {
    private val log = KotlinLogging.logger {}

    fun download(zippedDataFiles: List<ZippedDataFile>): Collection<HourlyMeasurement> {
        val measurementsByTime = HashMap<LocalDateTime, HourlyMeasurement>()
        for (dataFile in zippedDataFiles) {
            downloadBytes(dataFile.url)
                ?.let { findAndUnzipMeasurementFile(it) }
                ?.let { convertToHourlyMeasurements(it, dataFile.recordType, measurementsByTime) }
        }
        return measurementsByTime.values
    }

    private fun downloadBytes(url: URL): ByteArray? {
        log.info { "downloadBytes(${url})" }
        val bytes = url.openStream().use { it.readAllBytes() }
        log.info { "downloadBytes(${url}) finished, ${bytes.size} bytes" }
        return bytes
    }

    private fun findAndUnzipMeasurementFile(zippedBytes: ByteArray): SsvData? {
        log.info { "findAndUnzipMeasurementFile(${zippedBytes.size} bytes)" }
        val bytes = ZipInputStream(ByteArrayInputStream(zippedBytes)).use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsMeasurementFile(it.name) }) {
                SsvParser.parse(zipInputStream.bufferedReader())
            } else {
                null
            }
        }
        log.info { "findAndUnzipMeasurementFile(${zippedBytes.size} bytes) finished, ${bytes?.rows?.size} rows" }
        return bytes
    }

    private fun convertToHourlyMeasurements(
        ssvData: SsvData,
        recordType: RecordType,
        measurementByTime: HourlyMeasurementByTime
    ) {
        log.info { "convertToHourlyMeasurements(${ssvData.rows.size} rows, ${recordType}, ${measurementByTime.size} measurements by time)" }
        val indexMeasurementTime = ssvData.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        val columnMappingByIndex = recordType.columnNameMapping.mapKeys { ssvData.columnNames.indexOf(it.key) }
        for (row in ssvData.rows) {
            val measurementTimeString = row[indexMeasurementTime]
            val measurementTime = LocalDateTime.parse(measurementTimeString, DATE_TIME_FORMATTER)
            val record = measurementByTime.getOrPut(measurementTime) { HourlyMeasurement(measurementTime) }
            for (columnIndex in columnMappingByIndex) {
                val stringValue = row[columnIndex.key]
                if (stringValue != null) {
                    columnIndex.value.setValue(record, stringValue)
                }
            }
        }
        log.info { "convertToHourlyMeasurements(${ssvData.rows.size} rows, ${recordType}, ${measurementByTime.size} measurements by time) finished" }
    }

    private fun fileIsMeasurementFile(filename: String) = filename.startsWith("produkt_") && filename.endsWith(".txt")
}

object Summarizer {
    private val log = KotlinLogging.logger {}

    private val groupByFunctions =
        listOf(DateInterval::day, DateInterval::month, DateInterval::season, DateInterval::year, DateInterval::decade)

    fun summarizeMeasurements(stationId: StationId, measurements: HourlyMeasurements): SummarizedMeasurements {
        log.info { "summarizeMeasurements(${stationId}, ${measurements.size} measurements)" }
        val summarizedMeasurements = ArrayList<SummarizedMeasurement>()
        groupByFunctions.forEach { groupByFunction ->
            measurements.groupBy { groupByFunction(it.measurementTime) }
                .forEach { (interval, measurementsForInterval) ->
                    val summarizedMeasurement =
                        summarizeHourlyRecords(stationId, interval, measurementsForInterval)
                    summarizedMeasurements.add(summarizedMeasurement)
                }
        }
        log.info { "summarizeMeasurements(${stationId}, ${measurements.size} measurements) finished, ${summarizedMeasurements.size} summarized measurements" }
        return summarizedMeasurements
    }

    private fun summarizeHourlyRecords(
        stationId: StationId,
        interval: DateInterval,
        measurement: Collection<HourlyMeasurement>
    ) = SummarizedMeasurement(
        stationId = stationId,
        interval = interval,
        countCloudCoverage0 = measurement.count { it.cloudCoverage == 0 },
        countCloudCoverage1 = measurement.count { it.cloudCoverage == 1 },
        countCloudCoverage2 = measurement.count { it.cloudCoverage == 2 },
        countCloudCoverage3 = measurement.count { it.cloudCoverage == 3 },
        countCloudCoverage4 = measurement.count { it.cloudCoverage == 4 },
        countCloudCoverage5 = measurement.count { it.cloudCoverage == 5 },
        countCloudCoverage6 = measurement.count { it.cloudCoverage == 6 },
        countCloudCoverage7 = measurement.count { it.cloudCoverage == 7 },
        countCloudCoverage8 = measurement.count { it.cloudCoverage == 8 },
        countCloudCoverageNotVisible = measurement.count { it.cloudCoverage == -1 },
        countCloudCoverageNotMeasured = measurement.count { it.cloudCoverage == null },
        minDewPointTemperatureCentigrade = measurement.minDecimal { it.dewPointTemperatureCentigrade },
        avgDewPointTemperatureCentigrade = measurement.avgDecimal { it.dewPointTemperatureCentigrade },
        maxDewPointTemperatureCentigrade = measurement.maxDecimal { it.dewPointTemperatureCentigrade },
        minAirTemperatureCentigrade = measurement.minDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },
        avgAirTemperatureCentigrade = measurement.avgDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },
        maxAirTemperatureCentigrade = measurement.maxDecimal { it.airTemperatureAtTwoMetersHeightCentigrade }
    )
}

inline fun <T> Iterable<T>.minDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var minValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (minValue == null) {
            minValue = v
        } else if (minValue > v) {
            minValue = v
        }
    }
    return minValue
}

inline fun <T> Iterable<T>.maxDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (maxValue == null) {
            maxValue = v
        } else if (maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

inline fun <T> Collection<T>.avgDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var sum = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        v?.let { sum = sum?.add(v) }
    }
    return sum?.divide(BigDecimal.valueOf(size.toLong()), RoundingMode.HALF_UP)
}

private typealias HourlyMeasurementByTime = MutableMap<LocalDateTime, HourlyMeasurement>
private typealias SummarizedMeasurements = List<SummarizedMeasurement>

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH")
private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"
