package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.importer.converter.RecordConverter
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class UrlDirectoryReader(private val url: URL) {
    private var lines: List<String> = Collections.emptyList()

    private fun readContentFromUrlAsTextLines() {
        if (lines.isEmpty()) {
            url.openStream().use {
                val reader = BufferedReader(InputStreamReader(it))
                lines = reader.lines().collect(Collectors.toList())
            }
        }
    }

    fun <R : BaseRecord> createStreamForDownloadingAndConvertingZippedDataFiles(recordConverter: RecordConverter<R>): Stream<R> {
        readContentFromUrlAsTextLines()
        return lines.stream()
            .map(DATA_FILENAME_PATTERN::matcher)
            .filter(Matcher::find)
            .map(Matcher::group)
            .map { "$url/$it" }
            .map(::URL)
            .map(URL::openStream)
            .map(this::getDataFileInputStream)
            .map(::InputStreamReader)
            .map(::BufferedReader)
            .map(SsvParser::parse)
            .flatMap(recordConverter::convert)
    }

    fun downloadAndParseStationFile(): URL {
        readContentFromUrlAsTextLines()
        return lines.stream()
            .map(STATION_FILENAME_PATTERN::matcher)
            .filter(Matcher::find)
            .findFirst()
            .map { URL("$url/${it.group()}") }
            .orElse(null)
    }

    private fun getDataFileInputStream(inputStream: InputStream): InputStream {
        val zipInputStream = ZipInputStream(inputStream)
        var zipEntry: ZipEntry?
        while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
            val name: String? = zipEntry?.name
            if (fileIsDataFile(name)) {
                return zipInputStream
            }
        }
        return InputStream.nullInputStream()
    }

    private fun fileIsDataFile(filename: String?) =
        filename != null && filename.startsWith("produkt_") && filename.endsWith(".txt")
}

val DATA_FILENAME_PATTERN: Pattern =
    Pattern.compile("(?<group>stunden|tages)werte_(?<abbreviation>[A-Z]{2})_(?<stationId>\\d{5})_.*?(?<timePeriod>akt|hist)\\.zip")

val STATION_FILENAME_PATTERN: Pattern = Pattern.compile("[A-Z]{1,2}_(Stunden|Tages)werte_Beschreibung_Stationen.txt")
