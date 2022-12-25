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
        else "https://opendata.dwd.de/climate_environment/CDC/observations_germany/climate/"

    if (!baseUrlString.endsWith("/")) {
        baseUrlString += "/"
    }

    val hourlyUrl = URL(baseUrlString + "hourly/")
    val dailyUrl = URL(baseUrlString + "daily/kl/")

    val hourlyDirectory = HtmlDirectoryParser.parseHtml(hourlyUrl)
    val dailyDirectory = HtmlDirectoryParser.parseHtml(dailyUrl)

    WeatherDb.connect()

    val stationsFiles = hourlyDirectory.getAllStationsFiles() + dailyDirectory.getAllStationsFiles()
    importStations(stationsFiles)
    importMeasurements(hourlyDirectory, dailyDirectory)

    exitProcess(0)
}

