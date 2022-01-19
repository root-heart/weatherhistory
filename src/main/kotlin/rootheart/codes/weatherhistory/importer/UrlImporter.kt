package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import rootheart.codes.weatherhistory.database.DateInterval
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.importer.converter.BigDecimalProperty
import rootheart.codes.weatherhistory.importer.converter.IntProperty
import rootheart.codes.weatherhistory.importer.converter.PrecipitationTypeProperty
import rootheart.codes.weatherhistory.importer.html.HtmlDirectoryParser
import rootheart.codes.weatherhistory.importer.html.RecordType
import rootheart.codes.weatherhistory.importer.html.ZippedDataFile
import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import java.net.URL
import java.util.zip.ZipInputStream

private val log = KotlinLogging.logger {}

fun main(args: Array<String>) {
    var baseUrlString =
        if (args.size == 1) args[0]
        else "https://opendata.dwd.de/climate_environment/CDC/observations_germany/climate/hourly/"
    if (!baseUrlString.endsWith("/")) {
        baseUrlString += "/"
    }
    val baseUrl = URL(baseUrlString)
    val rootDirectory = HtmlDirectoryParser.parseHtml(baseUrl)
    val zippedDataFiles = rootDirectory.getAllZippedDataFiles()

    for ((stationId, dataFiles) in zippedDataFiles.groupBy { it.stationId }.filter { it.key.stationId == 691 }) {
        val records = HashMap<LocalDateTime, HourlyRecord>()
        for (dataFile in dataFiles) {
            parseRecords(dataFile, records)
        }
        log.info { "Parsed ${records.size} records for station $stationId" }
        val summarizedByDay = Summarizer.summarize(stationId, records.values) { DateInterval.day(it) }
        val summarizedByMonth = Summarizer.summarize(stationId, records.values) { DateInterval.month(it) }
        val summarizedBySeason = Summarizer.summarize(stationId, records.values) { DateInterval.season(it) }
        val summarizedByYear = Summarizer.summarize(stationId, records.values) { DateInterval.year(it) }
        val summarizedByDecade = Summarizer.summarize(stationId, records.values) { DateInterval.decade(it) }

        val importer = RecordsImporter(SummarizedMeasurement.tableMapping)

        importer.importEntities(summarizedByDay)
        importer.importEntities(summarizedByMonth)
        importer.importEntities(summarizedBySeason)
        importer.importEntities(summarizedByYear)
        importer.importEntities(summarizedByDecade)
    }
}

private val recordTypeColumnMapping = mapOf(
    RecordType.AIR_TEMPERATURE to mapOf(
        "TT_TU" to BigDecimalProperty(HourlyRecord::airTemperatureAtTwoMetersHeightCentigrade),
        "RF_TU" to BigDecimalProperty(HourlyRecord::relativeHumidityPercent)
    ),
    RecordType.CLOUD_TYPE to mapOf(
        "V_N" to IntProperty(HourlyRecord::cloudCoverage),
    ),
    RecordType.DEW_POINT to mapOf(
        "TD" to BigDecimalProperty(HourlyRecord::dewPointTemperatureCentigrade),
    ),
    RecordType.MAX_WIND_SPEED to mapOf(
        "FX_911" to BigDecimalProperty(HourlyRecord::maxWindSpeedMetersPerSecond)
    ),
    RecordType.MOISTURE to mapOf(
        "P_STD" to BigDecimalProperty(HourlyRecord::airPressureHectopascals),
    ),
    RecordType.PRECIPITATION to mapOf(
        "R1" to BigDecimalProperty(HourlyRecord::precipitationMillimeters),
        "WRTR" to PrecipitationTypeProperty(HourlyRecord::precipitationType)
    ),
    RecordType.SOIL_TEMPERATURE to mapOf(),
    RecordType.SUNSHINE_DURATION to mapOf(
        "SD_SO" to BigDecimalProperty(HourlyRecord::sunshineDuration)
    ),
    RecordType.VISIBILITY to mapOf(
        "V_VV" to BigDecimalProperty(HourlyRecord::visibilityInMeters)
    ),
    RecordType.WIND_SPEED to mapOf(
        "F" to BigDecimalProperty(HourlyRecord::windSpeedMetersPerSecond),
        "D" to BigDecimalProperty(HourlyRecord::windDirectionDegrees)
    )
)

private fun parseRecords(zippedDataFile: ZippedDataFile, records: MutableMap<LocalDateTime, HourlyRecord>) {
    log.info { "processing zip file at ${zippedDataFile.url}" }
    val columnMapping = recordTypeColumnMapping[zippedDataFile.recordType]
    if (columnMapping != null && columnMapping.isNotEmpty()) {
        ZipInputStream(zippedDataFile.url.openStream()).use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsDataFile(it.name) }) {
                log.info { "Found data file in ZIP" }
                val reader = zipInputStream.bufferedReader()
                val data = SsvParser.parse(reader)
                log.info { "Parsed data file" }
                log.info { "Column name mapping for type ${zippedDataFile.recordType} found: $columnMapping" }
                val indexMeasurementTime = data.columnNames.indexOf(COLUMN_NAME_MEASUREMENT_TIME)
                val columnIndices = columnMapping.mapKeys { data.columnNames.indexOf(it.key) }
                for (columnValues in data.columnValuesStream) {
                    val measurementTimeString = columnValues[indexMeasurementTime]
                    val measurementTime = LocalDateTime.parse(measurementTimeString, DATE_TIME_FORMATTER)
                    val record = records.computeIfAbsent(measurementTime) {
                        HourlyRecord(
                            zippedDataFile.stationId,
                            measurementTime
                        )
                    }
                    for (columnIndex in columnIndices) {
                        val stringValue = columnValues[columnIndex.key]
                        if (stringValue != null) {
                            columnIndex.value.setValue(record, stringValue)
                        }
                    }
                }
            }
            log.debug { "processed data file" }
        }
    }
}

// TODO copy paste from RecordConverter
private val DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH")
private const val COLUMN_NAME_STATION_ID = "STATIONS_ID"
private const val COLUMN_NAME_MEASUREMENT_TIME = "MESS_DATUM"

private fun fileIsDataFile(filename: String) =
    filename.startsWith("produkt_") && filename.endsWith(".txt")
