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
        static("web") { files(".") }
    }
}

private class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
    private val formatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd")
    override fun write(out: JsonWriter?, value: LocalDate?) {
        out?.value(formatter.print(value))
    }

    override fun read(`in`: JsonReader?): LocalDate {
        TODO("Not yet implemented")
    }
}

fun <T> PipelineContext<Unit, ApplicationCall>.requiredPathParam(name: String, map: (String) -> T?): T =
    map(requiredPathParam(name)) ?: badRequest("value for parameter $name cannot be mapped")

fun <T> PipelineContext<Unit, ApplicationCall>.optPathParam(name: String, map: (String) -> T?): T? =
    call.parameters[name]?.let { tryMapOrBadRequest(it, map) }

fun <T> PipelineContext<Unit, ApplicationCall>.optQueryParam(name: String, map: (String?) -> T?): T? =
    map(call.request.queryParameters[name])

private fun PipelineContext<Unit, ApplicationCall>.requiredPathParam(name: String): String =
    call.parameters[name] ?: badRequest("param $name required")


fun <T> tryMapOrBadRequest(value: String, map: (String) -> T): T? =
    try {
        map(value)
    } catch (e: Exception) {
        badRequest("value can not be mapped")
    }

fun badRequest(message: String): Nothing = throw throw BadRequestException(message)



