package rootheart.codes.weatherhistory.importer.html

import mu.KotlinLogging
import rootheart.codes.weatherhistory.model.StationId
import java.net.URL

private val log = KotlinLogging.logger {}

val DIRECTORY_NAME_REGEX =
    Regex("<a href=\"(?<directoryName>[A-Za-z0-9_]*)/\">")

val STATIONS_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(?<key>TU|CS|TD|FX|TF|EB|SD|VV|FF|RR)_(Stunden|Tages)werte_Beschreibung_Stationen.txt)\">")

val DATA_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(stunden|tages)werte_(?<key>(TU|CS|TD|FX|TF|EB|SD|VV|FF|RR))_(?<stationId>\\d{5})_.*?(?<recentness>akt|hist)\\.zip)\">")

data class HtmlDirectory(
    val url: URL,
    val subDirectories: List<HtmlDirectory>,
    val zippedDataFiles: List<ZippedDataFile>,
    val stationsFile: StationsFile?
) {

    fun getAllZippedDataFiles(resultList: MutableList<ZippedDataFile> = ArrayList()): List<ZippedDataFile> {
        resultList.addAll(zippedDataFiles)
        for (subDirectory in subDirectories) {
            subDirectory.getAllZippedDataFiles(resultList)
        }
        return resultList;
    }

}

enum class DataType(val abbreviation: String) {
    AIR_TEMPERATURE("TU"),
    CLOUD_TYPE("CS"),
    DEW_POINT("TD"),
    MAX_WIND_SPEED("FX"),
    MOISTURE("TF"),
    SOIL_TEMPERATURE("EB"),
    SUNSHINE_DURATION("SD"),
    VISIBILITY("VV"),
    WIND_SPEED("FF"),
    PRECIPITATION("RR");

    companion object {
        fun of(abbreviation: String): DataType = values().first { it.abbreviation == abbreviation }
    }
}

data class ZippedDataFile(
    val fileName: String,
    val stationId: StationId,
    val dataType: DataType,
    val historical: Boolean,
    val url: URL
)

data class StationsFile(
    val url: URL
)

object HtmlDirectoryParser {
    fun parseHtml(url: URL): HtmlDirectory {
        log.info { "Analyzing $url" }
        val htmlSource = url.readText()
        val directories = DIRECTORY_NAME_REGEX.findAll(htmlSource)
            .map { it.groups as MatchNamedGroupCollection }
            .map { it["directoryName"]!!.value }
            .filter { it != ".." }
            .map { URL("${url.toExternalForm()}$it/") }
            .map { parseHtml(it) }
            .toList()
        val zippedDataFiles = DATA_FILE_NAME_REGEX.findAll(htmlSource)
            .map { it.groups as MatchNamedGroupCollection }
            .map {
                val fileName = it["fileName"]!!.value
                ZippedDataFile(
                    fileName = fileName,
                    stationId = StationId.of(it["stationId"]!!.value),
                    dataType = DataType.of(it["key"]!!.value),
                    historical = it["recentness"]!!.value == "hist",
                    url = URL("${url.toExternalForm()}$fileName")
                )
            }
            .toList()
        val stationsFile = STATIONS_FILE_NAME_REGEX.findAll(htmlSource)
            .map { it.groups as MatchNamedGroupCollection }
            .map { it["fileName"]!!.value }
            .map { URL("${url.toExternalForm()}$it") }
            .map { StationsFile(it) }
            .firstOrNull()
        return HtmlDirectory(url, directories, zippedDataFiles, stationsFile)
    }
}