package rootheart.codes.weatherhistory.restapp

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import rootheart.codes.weatherhistory.database.WeatherDb
import rootheart.codes.weatherhistory.summary.summaryDataEndpoints


fun main() {
    WeatherDb.connect()
    val server = embeddedServer(Netty, port = 8080) {
        install(IgnoreTrailingSlash)
        install(ContentNegotiation) { gson() }
        setupRouting()
    }
    server.start(wait = true)
}

fun Application.setupRouting() = routing {
    stationsEndpoints()
    summaryDataEndpoints()
}
