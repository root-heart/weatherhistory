package rootheart.codes.weatherhistory.restapp

import io.ktor.application.*
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import rootheart.codes.common.measureAndLogDuration
import rootheart.codes.weatherhistory.database.Measurement
import rootheart.codes.weatherhistory.database.MeasurementDao
import rootheart.codes.weatherhistory.database.StationDao
import rootheart.codes.weatherhistory.database.TemperatureMeasurementDao
import rootheart.codes.weatherhistory.database.WeatherDb

private const val DATE_TIME_PATTERN = "yyyy-MM-dd"

fun main() {
    WeatherDb.connect()
    val server = embeddedServer(Netty, port = 8080) {
        install(IgnoreTrailingSlash)
        install(CORS) {
            anyHost()
        }
        install(ContentNegotiation) { gson() }
        setupRouting()
    }
    server.start(wait = true)
}

fun Application.setupRouting() = routing {
    static("web") {
        files(".")
    }

    get("stations") {
        call.respond(StationDao.findAll().sortedBy { it.federalState + it.name })
    }

    get("stations/{stationId}") {
        val stationId = call.parameters["stationId"]!!.toLong()
        StationDao.findById(stationId)
            ?.let { call.respond(it) }
            ?: call.respond(HttpStatusCode.NotFound, "Not Found")
    }

    route("summary/{stationId}") {
        get("{year}") {
            val stationId = call.parameters["stationId"]!!.toLong()
            val year = call.parameters["year"]!!.toInt()
            val summary = yearlySummary(stationId, year)
            call.respond(summary)
        }

        get("{year}/{month}") {
        }

        get("{year}/{month}/{day}") {
        }
    }

    route("temperature/{stationId}") {
        get("yearly") {

        }

        get("monthly/{year}") {

        }

        get("daily/{year}") {
            val stationId = call.parameters["stationId"]!!.toLong()
            val year = call.parameters["year"]!!.toInt()
            measureAndLogDuration("GET temperature/$stationId/daily/$year") {
                StationDao.findById(stationId)
                    ?.let { station -> TemperatureMeasurementDao.findDailyByYear(station, year) }
                    ?.let { measureAndLogDuration("Responding to GET temperature/$stationId/daily/$year") { call.respond(it) }}
                    ?: call.respond(HttpStatusCode.NotFound)
            }
        }

        get("daily/{year}/{month}") {
            val stationId = call.parameters["stationId"]!!.toLong()
            val year = call.parameters["year"]!!.toInt()
            val month = call.parameters["month"]!!.toInt()
            measureAndLogDuration("GET temperature/$stationId/daily/$year/$month") {
                StationDao.findById(stationId)
                    ?.let { station -> TemperatureMeasurementDao.findDailyByYearAndMonth(station, year, month) }
                    ?.let { measureAndLogDuration("Responding to GET temperature/$stationId/daily/$year/$month") { call.respond(it) }}
                    ?: call.respond(HttpStatusCode.NotFound)
            }
        }

        get("hourly/{year}/{month}/{day}") {

        }
    }

    route("dewPointTemperature") {
        // see "temperature" route above
    }

    route("humidity") {
        // see "temperature" route above
    }

    route("airPressure") {
        // see "temperature" route above
    }

    route("cloudCoverage") {
        // see "temperature" route above
    }

    route("sunshine") {
        // see "temperature" route above
    }

    route("rainfall") {
        // see "temperature" route above
    }

    route("snowfall") {
        // see "temperature" route above
    }

    route("wind") {
        // see "temperature" route above
    }

    route("visibility") {
        // see "temperature" route above
    }

    get("monthly/{stationId}/{year}") {
        // see "temperature" route above
    }

    route("daily/{stationId}") {
        get("{year}") {
            val stationId = call.parameters["stationId"]!!.toLong()
            val year = call.parameters["year"]!!.toInt()
            val dailyMeasurements = dailyMeasurements(stationId, year)
            call.respond(dailyMeasurements)
        }

        get("{year}/{month}") {
            val stationId = call.parameters["stationId"]!!.toLong()
            val year = call.parameters["year"]!!.toInt()
            val month = call.parameters["month"]!!.toInt()
            val dailyMeasurements = dailyMeasurements(stationId, year, month)
            call.respond(dailyMeasurements)
        }
    }

    get("hourly/{stationId}/{year}/{month}/{day}") {
    }

    stationsEndpoints()
//    summaryDataEndpoints()
//    measurementEndpoints()
}

fun dailyMeasurements(stationId: Long, year: Int): List<MeasurementJson> {
    val station = StationDao.findById(stationId)!!
    val dailyData = MeasurementDao.findByStationIdAndYear(station, year)
    return dailyData.map(Measurement::toJson).sortedBy(MeasurementJson::day)
}

fun dailyMeasurements(stationId: Long, year: Int, month: Int): List<MeasurementJson> {
    val station = StationDao.findById(stationId)!!
    val dailyData = MeasurementDao.findByStationIdAndYearAndMonth(station, year, month)
    return dailyData.map(Measurement::toJson).sortedBy(MeasurementJson::day)
}

private fun Measurement.toJson() = MeasurementJson(
    day = day.toString(DATE_TIME_PATTERN),

    hourlyAirTemperatureCentigrade = hourlyAirTemperatureCentigrade,
    minAirTemperatureCentigrade = minAirTemperatureCentigrade,
    avgAirTemperatureCentigrade = avgAirTemperatureCentigrade,
    maxAirTemperatureCentigrade = maxAirTemperatureCentigrade,

    hourlyDewPointTemperatureCentigrade = hourlyDewPointTemperatureCentigrade,
    minDewPointTemperatureCentigrade = minDewPointTemperatureCentigrade,
    maxDewPointTemperatureCentigrade = maxDewPointTemperatureCentigrade,
    avgDewPointTemperatureCentigrade = avgDewPointTemperatureCentigrade,

    hourlyHumidityPercent = hourlyHumidityPercent,
    minHumidityPercent = minHumidityPercent,
    avgHumidityPercent = avgHumidityPercent,
    maxHumidityPercent = maxHumidityPercent,

    hourlyAirPressureHectopascals = hourlyAirPressureHectopascals,
    minAirPressureHectopascals = minAirPressureHectopascals,
    avgAirPressureHectopascals = avgAirPressureHectopascals,
    maxAirPressureHectopascals = maxAirPressureHectopascals,

    hourlyCloudCoverages = hourlyCloudCoverages,

    hourlySunshineDurationMinutes = hourlySunshineDurationMinutes,
    sumSunshineDurationHours = sumSunshineDurationHours,

    hourlyRainfallMillimeters = hourlyRainfallMillimeters,
    sumRainfallMillimeters = sumRainfallMillimeters,

    hourlySnowfallMillimeters = hourlySnowfallMillimeters,
    sumSnowfallMillimeters = sumSnowfallMillimeters,

    hourlyWindSpeedMetersPerSecond = hourlyWindSpeedMetersPerSecond,
    avgWindSpeedMetersPerSecond = avgWindSpeedMetersPerSecond,
    maxWindSpeedMetersPerSecond = maxWindSpeedMetersPerSecond,

    hourlyWindDirectionDegrees = hourlyWindDirectionDegrees,

    hourlyVisibilityMeters = hourlyVisibilityMeters,
)

