package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.WeatherDb
import rootheart.codes.weatherhistory.restapp.resources.stationsResource
import rootheart.codes.weatherhistory.restapp.resources.measurementsResource

fun main() {
    WeatherDb.connect()
    val server = embeddedServer(Netty, port = 8080, module = Application::weatherHistory)
    server.start(wait = true)
}

private fun Application.weatherHistory() {
    install(IgnoreTrailingSlash)
    install(CORS) { anyHost() }
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            setDateFormat("yyyy-MM-dd")
        }
    }
    install(Resources)

    routing {
        stationsResource()
        measurementsResource()
        static("web") { files(".") }
    }
}
