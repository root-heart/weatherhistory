package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.HourlyMeasurement
import rootheart.codes.weatherhistory.database.Station
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.importer.html.ZippedDataFile
import rootheart.codes.weatherhistory.importer.ssv.SemicolonSeparatedValues
import rootheart.codes.weatherhistory.importer.ssv.SemicolonSeparatedValuesParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream


@DelicateCoroutinesApi
object DataFileForStationImporter {
    private val log = KotlinLogging.logger {}

    suspend fun import(scope: CoroutineScope, station: Station, zippedDataFiles: List<ZippedDataFile>) {
        log.info { "import(${station.id}, ${zippedDataFiles.size} zipped data files)" }
        val downloaded = Channel<Pair<ZippedDataFile, ByteArray>>(zippedDataFiles.size)
        launchUnzipper(scope, station, downloaded)
        download(zippedDataFiles, station, downloaded)
        log.info { "Station ${station.id} - Downloaded from ${zippedDataFiles.size} files" }
    }

    private fun launchUnzipper(
        scope: CoroutineScope,
        station: Station,
        downloaded: Channel<Pair<ZippedDataFile, ByteArray>>
    ) = scope.launch(CoroutineName("process-zipped-data-file")) {
        val measurements = convertToHourlyMeasurements(downloaded, station)
        HourlyMeasurementsImporter.importEntities(measurements)

        val groupedByDay = measurements.groupBy { DateInterval.day(it.measurementTime) }
        val summarizedByDay = groupedByDay.map { (day, measurements) ->
            Summarizer.summarizeHourlyRecords(station, day, measurements)
        }

        val groupedByMonth = summarizedByDay.groupBy { DateInterval.month(it.firstDay) }
        val summarizedByMonth = groupedByMonth.map { (month, measurements) ->
            Summarizer.summarizeSummarizedRecords(station, month, measurements)
        }

        val groupedByYear = summarizedByMonth.groupBy { DateInterval.year(it.firstDay) }
        val summarizedByYear = groupedByYear.map { (year, measurements) ->
            Summarizer.summarizeSummarizedRecords(station, year, measurements)
        }

        val groupedByDecade = summarizedByYear.groupBy { DateInterval.decade(it.firstDay) }
        val summarizedByDecade = groupedByDecade.map { (decade, measurements) ->
            Summarizer.summarizeSummarizedRecords(station, decade, measurements)
        }

        val summarizedMeasurements =
            summarizedByDay + summarizedByMonth + summarizedByYear + summarizedByDecade;
        SummarizedMeasurementImporter.importEntities(summarizedMeasurements)

        log.info { "Station ${station.id} - Converted: ${measurements.size}" }
    }

    private suspend fun download(
        zippedDataFiles: Collection<ZippedDataFile>,
        station: Station,
        downloaded: Channel<Pair<ZippedDataFile, ByteArray>>
    ) {
        zippedDataFiles.forEach { zippedDataFile ->
            log.info { "Station ${station.id} - Downloading from ${zippedDataFile.url}" }
            val content = zippedDataFile.url.readBytes()
            log.info { "Station ${station.id} - Downloaded from ${zippedDataFile.url}, ${content.size} bytes" }
            downloaded.send(Pair(zippedDataFile, content))
        }
        downloaded.close()
    }

    private suspend fun convertToHourlyMeasurements(
        downloaded: Channel<Pair<ZippedDataFile, ByteArray>>,
        station: Station
    ): Collection<HourlyMeasurement> {
        val measurementByTime = ConcurrentHashMap<DateTime, HourlyMeasurement>()
        coroutineScope {
            for (x in downloaded) {
                launch(CoroutineName("convert-zipped-data-file")) {
                    currentCoroutineContext().job
                    val zippedDataFile = x.first
                    val content = x.second
                    val unzippedContent = unzip(station, zippedDataFile, content)
                    val semicolonSeparatedValues = parse(station, zippedDataFile, unzippedContent)
                    convert(station, zippedDataFile, semicolonSeparatedValues, measurementByTime)
                }
            }
        }
        return measurementByTime.values
    }

    private fun unzip(station: Station, zippedDataFile: ZippedDataFile, zippedBytes: ByteArray): ByteArray? {
        log.info { "Station ${station.id} - Unzipping ${zippedDataFile.url}" }
        val unzippedContent = ZipInputStream(ByteArrayInputStream(zippedBytes))
            .use { zipInputStream ->
                val entries = generateSequence { zipInputStream.nextEntry }
                if (entries.any { fileIsMeasurementFile(it.name) }) {
                    return@use zipInputStream.readBytes()
                } else {
                    return@use null
                }
            }
        log.info { "Station ${station.id} - Unzipped ${zippedDataFile.url}, ${unzippedContent!!.size} bytes" }
        return unzippedContent
    }

    private fun parse(
        station: Station,
        zippedDataFile: ZippedDataFile,
        unzippedContent: ByteArray?
    ): SemicolonSeparatedValues {
        log.info { "Station ${station.id} - Parsing ${zippedDataFile.url}" }
        val inputStream = unzippedContent?.let(::ByteArrayInputStream) ?: InputStream.nullInputStream()
        val semicolonSeparatedValues = inputStream.bufferedReader().use(SemicolonSeparatedValuesParser::parse)
        log.info { "Station ${station.id} - Parsed ${zippedDataFile.url}, ${semicolonSeparatedValues.rows.size} rows" }
        return semicolonSeparatedValues
    }

    private fun convert(
        station: Station, zippedDataFile: ZippedDataFile, semicolonSeparatedValues: SemicolonSeparatedValues,
        measurementByTime: MutableMap<DateTime, HourlyMeasurement>
    ) {
        log.info { "Station ${station.id} - Converting ${zippedDataFile.url}" }
        val indexMeasurementTime = semicolonSeparatedValues.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
        val columnMappingByIndex = zippedDataFile.measurementType.columnNameMapping.mapKeys {
            semicolonSeparatedValues.columnNames.indexOf(it.key)
        }
        for (row in semicolonSeparatedValues.rows) {
            val measurementTimeString = row[indexMeasurementTime]
            val measurementTime = DATE_TIME_FORMATTER.parseDateTime(measurementTimeString)
            val record = measurementByTime.getOrPut(measurementTime) {
                HourlyMeasurement(station = station, measurementTime = measurementTime)
            }
            for (columnIndex in columnMappingByIndex) {
                val stringValue = row[columnIndex.key]
                if (stringValue != null) {
                    columnIndex.value.setValue(record, stringValue)
                }
            }
        }
        log.info { "Station ${station.id} - Converted ${zippedDataFile.url}" }
    }

    private fun fileIsMeasurementFile(filename: String) = filename.startsWith("produkt_") && filename.endsWith(".txt")
}

@DelicateCoroutinesApi
object Summarizer {
    fun summarizeSummarizedRecords(
        station: Station,
        interval: DateInterval,
        measurements: Collection<SummarizedMeasurement>
    ) = SummarizedMeasurement(
        station = station,
        interval = interval,
        countCloudCoverage0 = measurements.sumOf { it.countCloudCoverage0 },
        countCloudCoverage1 = measurements.sumOf { it.countCloudCoverage1 },
        countCloudCoverage2 = measurements.sumOf { it.countCloudCoverage2 },
        countCloudCoverage3 = measurements.sumOf { it.countCloudCoverage3 },
        countCloudCoverage4 = measurements.sumOf { it.countCloudCoverage4 },
        countCloudCoverage5 = measurements.sumOf { it.countCloudCoverage5 },
        countCloudCoverage6 = measurements.sumOf { it.countCloudCoverage6 },
        countCloudCoverage7 = measurements.sumOf { it.countCloudCoverage7 },
        countCloudCoverage8 = measurements.sumOf { it.countCloudCoverage8 },
        countCloudCoverageNotVisible = measurements.sumOf { it.countCloudCoverageNotVisible },
        countCloudCoverageNotMeasured = measurements.sumOf { it.countCloudCoverageNotMeasured },
        minDewPointTemperatureCentigrade = measurements.minDecimal { it.minDewPointTemperatureCentigrade },
        avgDewPointTemperatureCentigrade = measurements.avgDecimal { it.avgDewPointTemperatureCentigrade },
        maxDewPointTemperatureCentigrade = measurements.maxDecimal { it.maxDewPointTemperatureCentigrade },
        minAirTemperatureCentigrade = measurements.minDecimal { it.minAirTemperatureCentigrade },
        avgAirTemperatureCentigrade = measurements.avgDecimal { it.avgAirTemperatureCentigrade },
        maxAirTemperatureCentigrade = measurements.maxDecimal { it.maxAirTemperatureCentigrade },
        sumSunshineDurationHours = measurements.sumDecimal { it.sumSunshineDurationHours }
            ?.divide(SIXTY, RoundingMode.HALF_UP),
        details = ""
    )

    fun summarizeHourlyRecords(station: Station, interval: DateInterval, measurements: Collection<HourlyMeasurement>) =
        SummarizedMeasurement(station = station,
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

