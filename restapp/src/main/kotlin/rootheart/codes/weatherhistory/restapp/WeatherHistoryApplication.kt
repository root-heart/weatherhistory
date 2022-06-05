package rootheart.codes.weatherhistory.restapp

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.routing.IgnoreTrailingSlash
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import rootheart.codes.weatherhistory.database.WeatherDb


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
    stationsEndpoints()
    summaryDataEndpoints()
}
