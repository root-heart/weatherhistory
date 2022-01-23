package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import rootheart.codes.weatherhistory.importer.html.HtmlDirectoryParser
import java.net.URL

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

    zippedDataFiles.groupBy { it.stationId }.filter { it.key.stationId == 691 }.forEach {
        StationDataImporter.import(it.value)
    }
}
