package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.Resources
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.routing
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.database.WeatherDb
import rootheart.codes.weatherhistory.restapp.resources.stations.measurementsResource
import rootheart.codes.weatherhistory.restapp.resources.stations.stationsResource

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

val DATE_TIME_FORMAT = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd")

private class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter?, value: LocalDate?) {
        out?.value(if (value == null) "null" else DATE_TIME_FORMAT.print(value))
    }

    override fun read(`in`: JsonReader?): LocalDate {
        TODO("Not yet implemented")
    }
}


