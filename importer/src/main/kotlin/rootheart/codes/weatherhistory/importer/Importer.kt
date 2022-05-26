package rootheart.codes.weatherhistory.importer

import kotlinx.coroutines.DelicateCoroutinesApi
import mu.KotlinLogging
import rootheart.codes.weatherhistory.database.WeatherDb
import java.net.URL
import kotlin.system.exitProcess

private val log = KotlinLogging.logger {}

@DelicateCoroutinesApi
fun main(args: Array<String>) {
    var baseUrlString =
        if (args.size == 1) args[0]
        else "https://opendata.dwd.de/climate_environment/CDC/observations_germany/climate/hourly/"
    if (!baseUrlString.endsWith("/")) {
        baseUrlString += "/"
    }
    val baseUrl = URL(baseUrlString)
    val rootDirectory = HtmlDirectoryParser.parseHtml(baseUrl)

    WeatherDb.connect()

    importStations(rootDirectory)
    importMeasurements(rootDirectory)

    exitProcess(0)
}

