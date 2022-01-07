package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.importer.converter.RecordConverter
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.importer.ssv.SsvData
import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import rootheart.codes.weatherhistory.model.StationId
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.URL
import java.util.*
import java.util.stream.Stream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

//class UrlDirectoryReader(private val urlDirectory: UrlDirectory) {
//
//    fun <R : BaseRecord> downloadAndParseData(recordConverter: RecordConverter<R>): Map<StationId, Stream<R>> {
//        val result = HashMap<StationId, Stream<R>>()
//        val all = DATA_FILENAME_REGEX.findAll(directoryHtml)
//        for (matchResult in all) {
//            val groups = matchResult.groups as MatchNamedGroupCollection
//            val stationId = StationId.of(groups["stationId"]?.value ?: "")
//            val fileName = groups["fileName"]?.value ?: ""
//            getDataFileReader(fileName).use { reader ->
//                val d = SsvParser.parse(reader)
//                result[stationId] = recordConverter.convert(d)
//            }
//        }
//        return result
//    }
//
//    fun downloadAndParseStations(): List<DwdStation> {
//        val all = STATION_FILENAME_PATTERN.findAll(directoryHtml)
//        for (matchResult in all) {
//            val groups = matchResult.groups as MatchNamedGroupCollection
//            val fileName = groups["fileName"]?.value ?: ""
//            val stationFileLines = URL("$url/$fileName").readText().lines()
//            val columnNamesHeaderLine = stationFileLines[0]
//            for (i in (2..stationFileLines.size)) {
//                val line = stationFileLines[i]
//                val station = DwdStation(
//                    stationsId = StationId.of(line.substring(0, 5)),
//                    stationshoehe = Integer.parseInt(line.substring(24, 39).trim()),
//                    geoBreite = BigDecimal(line.substring(41, 50).trim()),
//                    geoLaenge = BigDecimal(line.substring(51, 60).trim()),
//                    stationsname = line.substring(61, 102).trim(),
//                    bundesland = line.substring(102).trim()
//                )
//            }
//        }
//        return Collections.emptyList()
//    }
//
//
//    private fun getStationFileReader(fileName: String): BufferedReader {
//        val fileUrl = URL("$url/$fileName")
//        return BufferedReader(InputStreamReader(url.openStream()))
//    }
//}

private val STATION_FILENAME_PATTERN =
    Regex("<a href=\"(?<fileName>[A-Z]{1,2}_(Stunden|Tages)werte_Beschreibung_Stationen.txt)\">")

val DATA_FILENAME_REGEX =
    Regex("<a href=\"(?<fileName>(stunden|tages)werte_([A-Z]{2})_(?<stationId>\\d{5})_.*?(akt|hist)\\.zip)\">")

//private fun getDataFileReader(fileName: String): BufferedReader {
//    val fileUrl = URL("$url/$fileName")
//    val zipInputStream = ZipInputStream(fileUrl.openStream())
//    var zipEntry: ZipEntry?
//    var inputStream = InputStream.nullInputStream()
//    while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
//        val name: String? = zipEntry?.name
//        if (fileIsDataFile(name)) {
//            println("Found data file ${zipEntry?.name}")
//            inputStream = zipInputStream
//            break
//        }
//    }
//    return BufferedReader(InputStreamReader(inputStream))
//}

private fun fileIsDataFile(filename: String) =
    filename.startsWith("produkt_") && filename.endsWith(".txt")

class UrlDirectoryReader(private val url: URL) {
    private val directoryHtml by lazy { url.readText() }

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

    fun forEachDataLine(consumer: (List<String>, List<String?>) -> Unit) {
        DATA_FILENAME_REGEX.findAll(directoryHtml)
            .map { it.groups as MatchNamedGroupCollection }
            .forEach { matchResult ->
                val href = URL(url.toString() + "/" + matchResult["fileName"]!!.value)
                ZipInputStream(href.openStream()).use { zipInputStream ->
                    val entries = generateSequence { zipInputStream.nextEntry }
                    if (entries.any { fileIsDataFile(it.name) }) {
                        zipInputStream.bufferedReader().use { reader ->
                            val columnNames = reader.readLine()?.split(";") ?: listOf()
                            reader.lines()
                                .map { it.split(";") }
                                .map { values -> values.map { nullify(it) } }
                                .forEach { consumer(columnNames, it) }
                        }
                    }
                }
            }
    }
}

@JvmInline
value class ColumnNames(val names: List<String>)

@JvmInline
value class ColumnValues(val values: List<String?>)

private fun nullify(value: String) = if (value == "-999") null else value

data class StationDataToImport(
    val station: Station
)

data class DataFile(val url: URL)

data class Station(
    var stationsId: StationId,
    val stationshoehe: Int,
    val geoBreite: BigDecimal,
    val geoLaenge: BigDecimal,
    val stationsname: String,
    val bundesland: String
)

data class DirectoryData<R>(
    val data: Map<StationId, Stream<R>>,
    val stations: List<Station>
)

private val COLUMN_NAMES_STATIONS_FILE = arrayOf(
    "Stations_id", "von_datum", "bis_datum", "Stationshoehe", "geoBreite", "geoLaenge", "Stationsname", "Bundesland"
)
