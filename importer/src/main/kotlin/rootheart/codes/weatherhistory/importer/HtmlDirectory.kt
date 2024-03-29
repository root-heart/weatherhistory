package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import java.net.URL

private val log = KotlinLogging.logger {}

val DIRECTORY_NAME_REGEX =
    Regex("<a href=\"(?<directoryName>\\w*)/\">")

val STATIONS_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(?<key>TU|N|TD|FX|TF|SD|VV|FF|RR|KL)_(Stunden|Tages)werte_Beschreibung_Stationen.txt)\">")

val DATA_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(stunden|tages)werte_(?<key>(TU|N|TD|FX|TF|SD|VV|FF|RR|KL))_(?<stationId>\\d{5})_.*?(?<recentness>akt|hist)\\.zip)\">.*\\s(?<size>\\d{1,10})")

data class HtmlDirectory(val url: URL,
                         val subDirectories: List<HtmlDirectory>,
                         val zippedDataFiles: List<ZippedDataFile>,
                         val stationsFile: StationsFile?) {
    fun getAllZippedDataFiles(resultList: MutableList<ZippedDataFile> = ArrayList()): List<ZippedDataFile> {
        resultList.addAll(zippedDataFiles)
        for (subDirectory in subDirectories) {
            subDirectory.getAllZippedDataFiles(resultList)
        }
        return resultList
    }

    fun getAllStationsFiles(resultList: MutableList<StationsFile> = ArrayList()): List<StationsFile> {
        stationsFile?.let { resultList.add(it) }
        for (subDirectory in subDirectories) {
            subDirectory.getAllStationsFiles(resultList)
        }
        return resultList
    }
}

data class ZippedDataFile(val fileName: String,
                          val externalId: String,
                          val measurementType: MeasurementType,
                          val historical: Boolean,
                          val url: URL,
                          val size: Long)

data class StationsFile(val filename: String,
                        val measurementType: MeasurementType,
                        val url: URL)

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
                    ZippedDataFile(fileName = fileName,
                                   externalId = it["stationId"]!!.value,
                                   measurementType = MeasurementType.of(it["key"]!!.value),
                                   historical = it["recentness"]!!.value == "hist",
                                   url = URL("${url.toExternalForm()}$fileName"),
                                   size = it["size"]!!.value.toLong())
                }
                .toList()
        val stationsFile = STATIONS_FILE_NAME_REGEX.findAll(htmlSource)
                .map { it.groups as MatchNamedGroupCollection }
                .map {
                    val filename = it["fileName"]!!.value
                    StationsFile(filename = filename,
                                 url = URL("${url.toExternalForm()}$filename"),
                                 measurementType = MeasurementType.of(it["key"]!!.value))
                }
                .firstOrNull()
        return HtmlDirectory(url, directories, zippedDataFiles, stationsFile)
    }
}