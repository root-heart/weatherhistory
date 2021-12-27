package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyAirTemperatureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyCloudTypeRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyDewPointTemperatureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyMaxWindSpeedRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyMoistureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyPrecipitationRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlySoilTemperatureRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlySunshineDurationRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyVisibilityRecordConverter
import rootheart.codes.weatherhistory.importer.converter.SsvToHourlyWindSpeedRecordConverter
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal
import java.net.URL
import java.util.stream.Stream

fun main(args: Array<String>) {
    val baseUrlString =
        if (args.size == 1) args[0]
        else "https://opendata.dwd.de/climate_environment/CDC/observations_germany/climate"
    crawlDwd(baseUrlString)
}

private fun crawlDwd(baseUrlString: String) {
    val urlSubDirectories = mapOf(
//    "daily/kl" to ,
        "hourly/air_temperature" to SsvToHourlyAirTemperatureRecordConverter,
        "hourly/cloud_type" to SsvToHourlyCloudTypeRecordConverter,
        "hourly/dew_point" to SsvToHourlyDewPointTemperatureRecordConverter,
        "hourly/extreme_wind" to SsvToHourlyMaxWindSpeedRecordConverter,
        "hourly/moisture" to SsvToHourlyMoistureRecordConverter,
        "hourly/precipitation" to SsvToHourlyPrecipitationRecordConverter,
        "hourly/soil_temperature" to SsvToHourlySoilTemperatureRecordConverter,
        "hourly/sun" to SsvToHourlySunshineDurationRecordConverter,
        "hourly/visibility" to SsvToHourlyVisibilityRecordConverter,
        "hourly/wind" to SsvToHourlyWindSpeedRecordConverter,
    )
    for ((subDirectory, recordConverter) in urlSubDirectories) {
        for (timePeriodDirectory in listOf("historical", "recent")) {
            val url = URL("$baseUrlString/$subDirectory/$timePeriodDirectory")
            val urlDirectoryReader = UrlDirectoryReader(url)
            val recordsStream =
                urlDirectoryReader.createStreamForDownloadingAndConvertingZippedDataFiles(recordConverter)
            insertRecordsIntoDatabase(recordsStream)

            val stationFile = urlDirectoryReader.downloadAndParseStationFile()
        }
    }
}

private fun <R : BaseRecord> insertRecordsIntoDatabase(records: Stream<R>) {

}

data class DwdStation(
    var stationsId: StationId,
    val stationshoehe: Int,
    val geoBreite: BigDecimal,
    val geoLaenge: BigDecimal,
    val stationsname: String,
    val bundesland: String
)
