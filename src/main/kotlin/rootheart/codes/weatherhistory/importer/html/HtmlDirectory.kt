package rootheart.codes.weatherhistory.importer.html

import mu.KotlinLogging
import rootheart.codes.weatherhistory.importer.HourlyMeasurement
import rootheart.codes.weatherhistory.importer.converter.BigDecimalProperty
import rootheart.codes.weatherhistory.importer.converter.IntProperty
import rootheart.codes.weatherhistory.importer.converter.PrecipitationTypeProperty
import rootheart.codes.weatherhistory.importer.converter.SimpleMeasurementProperty
import rootheart.codes.weatherhistory.model.StationId
import java.net.URL

private val log = KotlinLogging.logger {}

val DIRECTORY_NAME_REGEX =
    Regex("<a href=\"(?<directoryName>[A-Za-z0-9_]*)/\">")

val STATIONS_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(?<key>TU|CS|TD|FX|TF|SD|VV|FF|RR)_(Stunden|Tages)werte_Beschreibung_Stationen.txt)\">")

val DATA_FILE_NAME_REGEX =
    Regex("<a href=\"(?<fileName>(stunden|tages)werte_(?<key>(TU|CS|TD|FX|TF|SD|VV|FF|RR))_(?<stationId>\\d{5})_.*?(?<recentness>akt|hist)\\.zip)\">")

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
        return resultList
    }

}

enum class MeasurementType(
    val abbreviation: String,
    val columnNameMapping: Map<String, SimpleMeasurementProperty<HourlyMeasurement, *>>
) {
    AIR_TEMPERATURE(
        "TU", mapOf(
            "TT_TU" to BigDecimalProperty(HourlyMeasurement::airTemperatureAtTwoMetersHeightCentigrade),
            "RF_TU" to BigDecimalProperty(HourlyMeasurement::relativeHumidityPercent)
        )
    ),

//    CLOUD_TYPE( "CS", mapOf("V_N" to IntProperty(HourlyMeasurement::cloudCoverage))),

    CLOUDINESS("N", mapOf("V_N" to IntProperty(HourlyMeasurement::cloudCoverage))),

    DEW_POINT(
        "TD", mapOf(
            "TD" to BigDecimalProperty(HourlyMeasurement::dewPointTemperatureCentigrade),
        )
    ),

    MAX_WIND_SPEED("FX", mapOf("FX_911" to BigDecimalProperty(HourlyMeasurement::maxWindSpeedMetersPerSecond))),

    MOISTURE("TF", mapOf("P_STD" to BigDecimalProperty(HourlyMeasurement::airPressureHectopascals))),

//    SOIL_TEMPERATURE("EB", mapOf()),

    SUNSHINE_DURATION("SD", mapOf("SD_SO" to BigDecimalProperty(HourlyMeasurement::sunshineDuration))),

    VISIBILITY("VV", mapOf("V_VV" to BigDecimalProperty(HourlyMeasurement::visibilityInMeters))),

    WIND_SPEED(
        "FF", mapOf(
            "F" to BigDecimalProperty(HourlyMeasurement::windSpeedMetersPerSecond),
            "D" to BigDecimalProperty(HourlyMeasurement::windDirectionDegrees)
        )
    ),

    PRECIPITATION(
        "RR", mapOf(
            "R1" to BigDecimalProperty(HourlyMeasurement::precipitationMillimeters),
            "WRTR" to PrecipitationTypeProperty(HourlyMeasurement::precipitationType)
        )
    );

    companion object {
        fun of(abbreviation: String): MeasurementType = values().first { it.abbreviation == abbreviation }
    }
}

data class ZippedDataFile(
    val fileName: String,
    val stationId: StationId,
    val measurementType: MeasurementType,
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
                    measurementType = MeasurementType.of(it["key"]!!.value),
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