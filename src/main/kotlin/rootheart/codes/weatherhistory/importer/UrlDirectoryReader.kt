package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import rootheart.codes.weatherhistory.importer.converter.RecordConverter
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.model.StationId
import java.io.BufferedReader
import java.math.BigDecimal
import java.net.URL
import java.util.zip.ZipInputStream

private val STATION_FILENAME_PATTERN =
    Regex("<a href=\"(?<fileName>[A-Z]{1,2}_(Stunden|Tages)werte_Beschreibung_Stationen.txt)\">")

private val DATA_FILENAME_REGEX =
    Regex("<a href=\"(?<fileName>(stunden|tages)werte_([A-Z]{2})_(?<stationId>\\d{5})_.*?(akt|hist)\\.zip)\">")

private fun fileIsDataFile(filename: String) =
    filename.startsWith("produkt_") && filename.endsWith(".txt")

class UrlDirectoryReader<R : BaseRecord>(private val url: URL, private val recordConverter: RecordConverter<R>) {

    private val directoryHtml by lazy { url.readText() }
    private val log = KotlinLogging.logger {}

    fun forEachStation(consumer: (Station) -> Unit) {
        val groups = STATION_FILENAME_PATTERN.find(directoryHtml)?.groups as MatchNamedGroupCollection? ?: return
        val fileName = groups["fileName"]!!.value
        val stationFileLines = URL("$url/$fileName").readText().lines()
        for (i in (2..stationFileLines.size)) {
            val line = stationFileLines[i]
            val station = Station(
                stationsId = StationId.of(line.substring(0, 5)),
                stationshoehe = Integer.parseInt(line.substring(24, 39).trim()),
                geoBreite = BigDecimal(line.substring(41, 50).trim()),
                geoLaenge = BigDecimal(line.substring(51, 60).trim()),
                stationsname = line.substring(61, 102).trim(),
                bundesland = line.substring(102).trim()
            )
            consumer(station)
        }
    }

    fun forEachRecord(processRecord: (R) -> Unit) {
        log.debug { "searching for hrefs in directory HTML in $url" }
        val matchResults = DATA_FILENAME_REGEX.findAll(directoryHtml).map { it.groups as MatchNamedGroupCollection }
        for (matchResult in matchResults) {
            val href = url.toString() + "/" + matchResult["fileName"]!!.value
            log.debug { "found href $href" }
            val zipUrl = URL(href)
            processZipFile(zipUrl, processRecord)
        }
    }

    private fun processZipFile(zipUrl: URL, processRecord: (R) -> Unit) {
        ZipInputStream(zipUrl.openStream()).use { zipInputStream ->
            val entries = generateSequence { zipInputStream.nextEntry }
            if (entries.any { fileIsDataFile(it.name) }) {
                log.debug { "Found data file in ZIP" }
                processDataFile(zipInputStream, processRecord)
                log.debug { "processed data file" }
            }
        }
    }


    private fun processDataFile(zipInputStream: ZipInputStream, processRecord: (R) -> Unit) {
        zipInputStream.bufferedReader().let { reader ->
            val header = reader.readLine() ?: return
            val columnNames = splitAndTrimTokens(header)
            recordConverter.validateColumnNames(columnNames)
            recordConverter.determineIndicesOfColumnsAlwaysPresent(columnNames)
            parseLines(reader, processRecord)
        }
    }

    private fun parseLines(reader: BufferedReader, processRecord: (R) -> Unit) {
        val values = ArrayList<String>()
        for (line in reader.lines()) {
            splitAndTrimTokens(line, values)
            val record = recordConverter.createRecord(values)
            processRecord(record)
            values.clear()
        }
    }
}

fun splitAndTrimTokens(line: String, list: MutableList<String> = ArrayList()): List<String> {
    var pos = 0
    var end = line.indexOf(';', pos)
    while (end >= 0) {
        while (line[pos] == ' ') {
            pos++
        }
        while (line[end - 1] == ' ') {
            end--
        }
        val column = line.substring(pos, end)
        list.add(column)
        pos = end + 1
        end = line.indexOf(';', pos)
    }
    return list
}

data class DataFile(val url: URL)

data class Station(
    var stationsId: StationId,
    val stationshoehe: Int,
    val geoBreite: BigDecimal,
    val geoLaenge: BigDecimal,
    val stationsname: String,
    val bundesland: String
)

private val COLUMN_NAMES_STATIONS_FILE = arrayOf(
    "Stations_id", "von_datum", "bis_datum", "Stationshoehe", "geoBreite", "geoLaenge", "Stationsname", "Bundesland"
)
