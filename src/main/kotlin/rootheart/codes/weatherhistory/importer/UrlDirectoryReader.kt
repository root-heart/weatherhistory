package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.importer.converter.RecordConverter
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import rootheart.codes.weatherhistory.model.StationId
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.URL
import java.util.stream.Stream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class UrlDirectoryReader(private val url: URL) {
    private var lines: String = ""

    private fun readContentFromUrlAsTextLines() {
        if (lines.isEmpty()) {
            lines = url.readText()
        }
    }

    fun <R : BaseRecord> downloadAndParseData(recordConverter: RecordConverter<R>): Map<StationId, Stream<R>> {
        readContentFromUrlAsTextLines()
        val result = HashMap<StationId, Stream<R>>()
        val all = DATA_FILENAME_REGEX.findAll(lines)
        for (matchResult in all) {
            val groups = matchResult.groups as MatchNamedGroupCollection
            val stationId = StationId.of(groups["stationId"]?.value ?: "")
            val fileName = groups["fileName"]?.value ?: ""
            val r = getDataFileReader(fileName)
            val d = SsvParser.parse(r)
            result[stationId] = recordConverter.convert(d)
        }
        return result
    }

//    fun downloadAndParseStationFile(): URL {
//        readContentFromUrlAsTextLines()
//        return lines.stream()
//            .map(STATION_FILENAME_PATTERN::matcher)
//            .filter(Matcher::find)
//            .findFirst()
//            .map { URL("$url/${it.group()}") }
//            .orElse(null)
//    }

    private fun getDataFileReader(filename: String): BufferedReader {
        val fileUrl = URL("$url/$filename")
        val zipInputStream = ZipInputStream(fileUrl.openStream())
        var zipEntry: ZipEntry?
        var inputStream = InputStream.nullInputStream()
        while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
            val name: String? = zipEntry?.name
            if (fileIsDataFile(name)) {
                println("Found data file ${zipEntry?.name}")
                inputStream = zipInputStream
                break
            }
        }
        return BufferedReader(InputStreamReader(inputStream))
    }

    private fun fileIsDataFile(filename: String?) =
        filename != null && filename.startsWith("produkt_") && filename.endsWith(".txt")
}

data class DwdStation(
    var stationsId: StationId,
    val stationshoehe: Int,
    val geoBreite: BigDecimal,
    val geoLaenge: BigDecimal,
    val stationsname: String,
    val bundesland: String
)

data class DirectoryData<R>(
    val data: Map<StationId, Stream<R>>,
    val stations: List<DwdStation>
)

val DATA_FILENAME_REGEX =
    Regex("<a href=\"(?<fileName>(?<group>stunden|tages)werte_(?<abbreviation>[A-Z]{2})_(?<stationId>\\d{5})_.*?(?<timePeriod>akt|hist)\\.zip)\">")

val STATION_FILENAME_PATTERN =
    Regex("<a href=\"(?<fileName>[A-Z]{1,2}_(Stunden|Tages)werte_Beschreibung_Stationen.txt)\">")
