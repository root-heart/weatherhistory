package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import rootheart.codes.weatherhistory.database.SummarizedMeasurement
import rootheart.codes.weatherhistory.importer.converter.HourlyAirTemperatureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlyCloudTypeRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlyDewPointTemperatureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlyMaxWindSpeedRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlyMoistureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlyPrecipitationRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlySoilTemperatureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlySunshineDurationRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlyVisibilityRecordConverter
import rootheart.codes.weatherhistory.importer.converter.HourlyWindSpeedRecordConverter
import rootheart.codes.weatherhistory.importer.converter.RecordConverter
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.importer.records.HourlyAirTemperatureRecord
import rootheart.codes.weatherhistory.importer.records.HourlyCloudTypeRecord
import rootheart.codes.weatherhistory.importer.records.HourlyDewPointTemperatureRecord
import rootheart.codes.weatherhistory.importer.records.HourlyMaxWindSpeedRecord
import rootheart.codes.weatherhistory.importer.records.HourlyMoistureRecord
import rootheart.codes.weatherhistory.importer.records.HourlyPrecipitationRecord
import rootheart.codes.weatherhistory.importer.records.HourlySoilTemperatureRecord
import rootheart.codes.weatherhistory.importer.records.HourlySunshineDurationRecord
import rootheart.codes.weatherhistory.importer.records.HourlyVisibilityRecord
import rootheart.codes.weatherhistory.importer.records.HourlyWindSpeedRecord
import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import rootheart.codes.weatherhistory.model.PrecipitationType
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDateTime
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KProperty0

fun main(args: Array<String>) {
    val baseUrlString =
        if (args.size == 1) args[0]
        else "https://opendata.dwd.de/climate_environment/CDC/observations_germany/climate"
    crawlDwd(baseUrlString)
}

private val log = KotlinLogging.logger {}

private val DATA_FILENAME_REGEX =
    Regex("<a href=\"(?<fileName>(stunden|tages)werte_(?<key>[A-Z]{2})_(?<stationId>\\d{5})_.*?(?<recentness>akt|hist)\\.zip)\">")

private val urlSubDirectories = listOf(
//    "daily/kl" to ,
    "hourly/air_temperature",
    "hourly/cloud_type",
    "hourly/dew_point",
    "hourly/extreme_wind",
    "hourly/moisture",
    "hourly/precipitation",
    "hourly/soil_temperature",
    "hourly/sun",
    "hourly/visibility",
    "hourly/wind",
)

private fun crawlDwd(baseUrlString: String) {
    val zipDataFileUrlsByStation = findZipDataFileUrlsByStation(baseUrlString)
    for ((stationId, dataFileUrls) in zipDataFileUrlsByStation) {
        val records = downloadAndParseDataFiles(dataFileUrls)
        val mergedHourlyRecords = mergeHourlyRecords(stationId, records)
        log.info { "Station ID $stationId, ${mergedHourlyRecords.size} merged records" }
    }
}

private fun findZipDataFileUrlsByStation(baseUrlString: String): Map<StationId, DataFileUrls> {
    val zipDataFileUrlsByStation = HashMap<StationId, DataFileUrls>()
    for (subDirectory in urlSubDirectories) {
        for (timePeriodDirectory in listOf("historical", "recent")) {
            val urlString = "$baseUrlString/$subDirectory/$timePeriodDirectory"
            parseHtml(urlString, zipDataFileUrlsByStation)
        }
    }
    return zipDataFileUrlsByStation
}

private fun parseHtml(urlString: String, zipDataFileUrlsByStation: MutableMap<StationId, DataFileUrls>) {
    log.info { "searching for hrefs in directory HTML in $urlString" }
    val directoryHtml = URL(urlString).readText()
    val groups = DATA_FILENAME_REGEX.findAll(directoryHtml)
        .map { it.groups as MatchNamedGroupCollection }
    for (group in groups) {
        val stationId = StationId.of(group["stationId"]!!.value)
        val filename = group["fileName"]!!.value
        val recentness = group["recentness"]!!.value
        val zipUrl = URL("$urlString/$filename")
        val dataFiles = zipDataFileUrlsByStation.computeIfAbsent(stationId) { DataFileUrls() }
        val property = when (group["key"]!!.value) {
            "TU" -> dataFiles::airTemperatureUrls
            "CS" -> dataFiles::cloudTypeUrls
            "TD" -> dataFiles::dewPointTemperatureUrls
            "FX" -> dataFiles::maxWindSpeedUrls
            "TF" -> dataFiles::moistureUrls
            "EB" -> dataFiles::soilTemperatureUrls
            "SD" -> dataFiles::sunshineDurationUrls
            "VV" -> dataFiles::visibilityUrls
            "FF" -> dataFiles::windSpedUrls
            "RR" -> dataFiles::precipitationUrls
            else -> throw IllegalArgumentException()
        }
        val propertyValue = property.get()
        if (recentness == "hist") {
            propertyValue.historical = zipUrl
        } else if (recentness == "akt") {
            propertyValue.recent = zipUrl
        }
    }
}

data class ZipUrls(var recent: URL? = null, var historical: URL? = null)

val airTemperatureParser = RecordsParser(HourlyAirTemperatureRecordConverter)
val cloudTypeParser = RecordsParser(HourlyCloudTypeRecordConverter)
val dewPointTemperatureParser = RecordsParser(HourlyDewPointTemperatureRecordConverter)
val maxWindSpeedParser = RecordsParser(HourlyMaxWindSpeedRecordConverter)
val moistureParser = RecordsParser(HourlyMoistureRecordConverter)
val precipitationParser = RecordsParser(HourlyPrecipitationRecordConverter)
val soilTemperatureParser = RecordsParser(HourlySoilTemperatureRecordConverter)
val sunshineDurationParser = RecordsParser(HourlySunshineDurationRecordConverter)
val visibilityParser = RecordsParser(HourlyVisibilityRecordConverter)
val windSpeedParser = RecordsParser(HourlyWindSpeedRecordConverter)

private fun downloadAndParseDataFiles(urls: DataFileUrls) = DataFileRecords(
    airTemperatureRecords = airTemperatureParser.parse(urls.airTemperatureUrls),
    cloudTypeRecords = cloudTypeParser.parse(urls.cloudTypeUrls),
    dewPointTemperatureRecords = dewPointTemperatureParser.parse(urls.dewPointTemperatureUrls),
    maxWindSpeedRecords = maxWindSpeedParser.parse(urls.maxWindSpeedUrls),
    moistureRecords = moistureParser.parse(urls.moistureUrls),
    precipitationRecords = precipitationParser.parse(urls.precipitationUrls),
    soilTemperatureRecords = soilTemperatureParser.parse(urls.soilTemperatureUrls),
    sunshineDurationRecords = sunshineDurationParser.parse(urls.sunshineDurationUrls),
    visibilityRecords = visibilityParser.parse(urls.visibilityUrls),
    windSpedRecords = windSpeedParser.parse(urls.windSpedUrls)
)

// TODO creating separate records and then merging them seconds after creating is wasting many CPU cycles...
private fun mergeHourlyRecords(stationId: StationId, records: DataFileRecords): Collection<HourlyRecord> {
    val hourlyRecords = HashMap<LocalDateTime, HourlyRecord>()
    for (r in records.airTemperatureRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.airTemperatureAtTwoMetersHeightCentigrade = r.airTemperatureAtTwoMetersHeightCentigrade
        hourlyRecord.relativeHumidityPercent = r.relativeHumidityPercent
    }
    for (r in records.cloudTypeRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.cloudCoverage = r.overallCoverage
    }
    for (r in records.dewPointTemperatureRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.dewPointTemperatureCentigrade = r.dewPointTemperatureCentigrade
    }
    for (r in records.maxWindSpeedRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.maxWindSpeedMetersPerSecond = r.maxWindSpeedMetersPerSecond
    }
    for (r in records.moistureRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.airPressureHectopascals = r.airPressureHectopascals
    }
    for (r in records.precipitationRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.precipitationMillimeters = r.precipitationMillimeters
        hourlyRecord.precipitationType = r.precipitationType
    }
    for (r in records.soilTemperatureRecords) {
        // ??
    }
    for (r in records.sunshineDurationRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.sunshineDuration = r.sunshineDuration
    }
    for (r in records.visibilityRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.visibilityInMeters = r.visibilityInMeters
    }
    for (r in records.windSpedRecords) {
        val hourlyRecord = hourlyRecords.computeIfAbsent(r.measurementTime!!) { HourlyRecord(stationId, it) }
        hourlyRecord.windSpeedMetersPerSecond = r.windSpeedMetersPerSecond
        hourlyRecord.windDirectionDegrees = r.windDirectionDegrees
    }
    return hourlyRecords.values
}

//private fun summarize(records: DataFileRecords): List<SummarizedMeasurement> {
//
//}
//
//private fun saveToDatabase(records: List<SummarizedMeasurement>) {
//
//}

class RecordsParser<R : BaseRecord>(val recordConverter: RecordConverter<R>) {
    fun parse(zipDataFileUrls: ZipUrls?): List<R> {
        if (zipDataFileUrls == null) return Collections.emptyList()
        val records = ArrayList<R>()
        if (zipDataFileUrls.recent != null) {
            parseRecordsAndAddToList(zipDataFileUrls.recent!!, records)
        }
        if (zipDataFileUrls.historical != null) {
            parseRecordsAndAddToList(zipDataFileUrls.historical!!, records)
        }
        return records
    }

    private fun parseRecordsAndAddToList(zipDataFileUrl: URL, records: MutableList<R>) {
        log.info { "processing zip file at $zipDataFileUrl" }
        ZipInputStream(zipDataFileUrl.openStream()).use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsDataFile(it.name) }) {
                log.debug { "Found data file in ZIP" }
                val reader = zipInputStream.bufferedReader()
                val data = SsvParser.parse(reader)
                for (record in recordConverter.convert(data)) {
                    records.add(record)
                }
                log.debug { "processed data file" }
            }
        }
    }
}


private fun fileIsDataFile(filename: String) =
    filename.startsWith("produkt_") && filename.endsWith(".txt")

data class DataFileUrls(
    val airTemperatureUrls: ZipUrls = ZipUrls(),
    val cloudTypeUrls: ZipUrls = ZipUrls(),
    val dewPointTemperatureUrls: ZipUrls = ZipUrls(),
    val maxWindSpeedUrls: ZipUrls = ZipUrls(),
    val moistureUrls: ZipUrls = ZipUrls(),
    val precipitationUrls: ZipUrls = ZipUrls(),
    val soilTemperatureUrls: ZipUrls = ZipUrls(),
    val sunshineDurationUrls: ZipUrls = ZipUrls(),
    val visibilityUrls: ZipUrls = ZipUrls(),
    val windSpedUrls: ZipUrls = ZipUrls(),
)

data class DataFileRecords(
    val airTemperatureRecords: List<HourlyAirTemperatureRecord>,
    val cloudTypeRecords: List<HourlyCloudTypeRecord>,
    val dewPointTemperatureRecords: List<HourlyDewPointTemperatureRecord>,
    val maxWindSpeedRecords: List<HourlyMaxWindSpeedRecord>,
    val moistureRecords: List<HourlyMoistureRecord>,
    val precipitationRecords: List<HourlyPrecipitationRecord>,
    val soilTemperatureRecords: List<HourlySoilTemperatureRecord>,
    val sunshineDurationRecords: List<HourlySunshineDurationRecord>,
    val visibilityRecords: List<HourlyVisibilityRecord>,
    val windSpedRecords: List<HourlyWindSpeedRecord>,
)
