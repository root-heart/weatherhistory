package rootheart.codes.weatherhistory.importer.html

import java.net.URL


val DIRECTORY_NAME_REGEX =
    Regex("<a href=\"(?<directoryName>[A-Za-z0-9_]*)/\">")

val STATIONS_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(?<key>TU|CS|TD|FX|TF|EB|SD|VV|FF|RR)_(Stunden|Tages)werte_Beschreibung_Stationen.txt)\">")

val DATA_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(stunden|tages)werte_(?<key>(TU|CS|TD|FX|TF|EB|SD|VV|FF|RR))_(?<stationId>\\d{5})_.*?(?<recentness>akt|hist)\\.zip)\">")

data class HtmlDirectory(
    val subDirectories: List<HtmlDirectory>,
    val zippedDataFiles: List<ZippedDataFile>,
    val stationsFile: StationsFile?
)

data class ZippedDataFile(
    val url: URL
)

data class StationsFile(
    val url: URL
)

object HtmlDirectoryParser {
    fun parseHtml(url: URL): HtmlDirectory {
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
            .map { it["fileName"]!!.value }
            .map { URL("${url.toExternalForm()}$it") }
            .map { ZippedDataFile(it) }
            .toList()
        val stationsFile = STATIONS_FILE_NAME_REGEX.findAll(htmlSource)
            .map { it.groups as MatchNamedGroupCollection }
            .map { it["fileName"]!!.value }
            .map { URL("${url.toExternalForm()}$it") }
            .map { StationsFile(it) }
            .firstOrNull()
        return HtmlDirectory(directories, zippedDataFiles, stationsFile)
    }
}