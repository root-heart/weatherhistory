package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.HourlyMeasurements
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.model.MeasurementType
import rootheart.codes.weatherhistory.importer.html.ZippedDataFile
import rootheart.codes.weatherhistory.importer.ssv.SsvData
import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import rootheart.codes.weatherhistory.model.StationId
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream


@DelicateCoroutinesApi
object DataFileForStationImporter {
    private val log = KotlinLogging.logger {}

    fun import(zippedDataFiles: List<ZippedDataFile>) {
        log.info { "import(${zippedDataFiles.size} zipped data files)" }
        val stationId = getStationId(zippedDataFiles)
        val measurements = Downloader.download(zippedDataFiles)
        HourlyMeasurementsImporter.importEntities(measurements)
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

@DelicateCoroutinesApi
object Downloader {
    private val log = KotlinLogging.logger {}
    private val downloadThreadPool = newFixedThreadPoolContext(2, "downloader")

    fun download(zippedDataFiles: List<ZippedDataFile>): Collection<HourlyMeasurement> {
        val measurementsByTime = ConcurrentHashMap<DateTime, HourlyMeasurement>()
        runBlocking {
            for (dataFile in zippedDataFiles) {
                launch(downloadThreadPool) {
                    downloadBytes(dataFile.url)
                        ?.let { findAndUnzipMeasurementFile(it) }
                        ?.let { convertToHourlyMeasurements(it, dataFile.measurementType, measurementsByTime) }
                }
            }
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
        measurementType: MeasurementType,
        measurementByTime: HourlyMeasurementByTime
    ) {
        log.info { "convertToHourlyMeasurements(${ssvData.rows.size} rows, ${measurementType}, ${measurementByTime.size} measurements by time)" }
        val indexMeasurementTime = ssvData.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        val columnMappingByIndex = measurementType.columnNameMapping.mapKeys { ssvData.columnNames.indexOf(it.key) }
        for (row in ssvData.rows) {
            val measurementTimeString = row[indexMeasurementTime]
            val measurementTime = DATE_TIME_FORMATTER.parseDateTime(measurementTimeString)
            val record = measurementByTime.getOrPut(measurementTime) { HourlyMeasurement(measurementTime) }
            for (columnIndex in columnMappingByIndex) {
                val stringValue = row[columnIndex.key]
                if (stringValue != null) {
                    columnIndex.value.setValue(record, stringValue)
                }
            }
        }
        log.info { "convertToHourlyMeasurements(${ssvData.rows.size} rows, ${measurementType}, ${measurementByTime.size} measurements by time) finished" }
    }

    private fun fileIsMeasurementFile(filename: String) = filename.startsWith("produkt_") && filename.endsWith(".txt")
}

@DelicateCoroutinesApi
object Summarizer {
    private val log = KotlinLogging.logger {}
    private val summarizeThreadPool = newFixedThreadPoolContext(10, "summarizer")

    private val groupByFunctions =
        listOf(DateInterval::day, DateInterval::month, DateInterval::season, DateInterval::year, DateInterval::decade)

    fun summarizeMeasurements(stationId: StationId, measurements: HourlyMeasurements): SummarizedMeasurements {
        log.info { "summarizeMeasurements(${stationId}, ${measurements.size} measurements)" }
        val summarizedMeasurements = ArrayList<SummarizedMeasurement>()
        runBlocking {
            for (groupByFunction in groupByFunctions) {
                val measurementsGroupedByInterval = measurements.groupBy { groupByFunction(it.measurementTime) }
                measurementsGroupedByInterval.forEach { (interval, measurements) ->
                    launch(summarizeThreadPool) {
                        val summarized = summarizeHourlyRecords(stationId, interval, measurements)
                        synchronized(summarizedMeasurements) {
                            summarizedMeasurements.add(summarized)
                        }
                    }
                }
            }
        }
        log.info { "summarizeMeasurements(${stationId}, ${measurements.size} measurements) finished, ${summarizedMeasurements.size} summarized measurements" }
        return summarizedMeasurements
    }

    private fun summarizeHourlyRecords(stationId: StationId, interval: DateInterval, measurements: HourlyMeasurements) =
        SummarizedMeasurement(stationId = stationId,
            interval = interval,
            countCloudCoverage0 = measurements.count { it.cloudCoverage == 0 },
            countCloudCoverage1 = measurements.count { it.cloudCoverage == 1 },
            countCloudCoverage2 = measurements.count { it.cloudCoverage == 2 },
            countCloudCoverage3 = measurements.count { it.cloudCoverage == 3 },
            countCloudCoverage4 = measurements.count { it.cloudCoverage == 4 },
            countCloudCoverage5 = measurements.count { it.cloudCoverage == 5 },
            countCloudCoverage6 = measurements.count { it.cloudCoverage == 6 },
            countCloudCoverage7 = measurements.count { it.cloudCoverage == 7 },
            countCloudCoverage8 = measurements.count { it.cloudCoverage == 8 },
            countCloudCoverageNotVisible = measurements.count { it.cloudCoverage == -1 },
            countCloudCoverageNotMeasured = measurements.count { it.cloudCoverage == null },
            minDewPointTemperatureCentigrade = measurements.minDecimal { it.dewPointTemperatureCentigrade },
            avgDewPointTemperatureCentigrade = measurements.avgDecimal { it.dewPointTemperatureCentigrade },
            maxDewPointTemperatureCentigrade = measurements.maxDecimal { it.dewPointTemperatureCentigrade },
            minAirTemperatureCentigrade = measurements.minDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },
            avgAirTemperatureCentigrade = measurements.avgDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },
            maxAirTemperatureCentigrade = measurements.maxDecimal { it.airTemperatureAtTwoMetersHeightCentigrade },
            sumSunshineDurationHours = measurements.sumDecimal { it.sunshineDurationMinutes }
                ?.divide(SIXTY, RoundingMode.HALF_UP)
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
        } else if (v != null && minValue > v) {
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
        } else if (v != null && maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

inline fun <T> Collection<T>.avgDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    return sumDecimal(selector)?.divide(BigDecimal.valueOf(size.toLong()), RoundingMode.HALF_UP)
}

inline fun <T> Collection<T>.sumDecimal(selector: (T) -> BigDecimal?): BigDecimal? {
    val iterator = iterator()
    if (!iterator.hasNext()) throw NoSuchElementException()
    var sum = selector(iterator.next())
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        v?.let { sum = sum?.add(v) ?: v }
    }
    return sum
}

private typealias HourlyMeasurementByTime = MutableMap<DateTime, HourlyMeasurement>
private typealias SummarizedMeasurements = List<SummarizedMeasurement>

private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC()
private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"

private val SIXTY = BigDecimal.valueOf(60)

