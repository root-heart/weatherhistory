package rootheart.codes.weatherhistory.restapp

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.util.pipeline.*
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years


inline fun <reified T> PipelineContext<Unit, ApplicationCall>.requiredPathParam(name: String, map: (String) -> T?): T =
    map(requiredPathParam(name)) ?: badRequest("value for parameter $name cannot be mapped to ${T::class}")

inline fun <reified T> PipelineContext<Unit, ApplicationCall>.optPathParam(name: String,
                                                                           noinline map: (String) -> T?): T? =
    call.parameters[name]?.let { tryMapOrBadRequest(it, map) }

fun PipelineContext<Unit, ApplicationCall>.optQueryParam(name: String): String? =
    call.request.queryParameters[name]

fun <T> PipelineContext<Unit, ApplicationCall>.optQueryParam(name: String, map: (String?) -> T?): T? =
    call.request.queryParameters[name]?.let { tryMapOrBadRequest(it, map) }

fun PipelineContext<Unit, ApplicationCall>.requiredPathParam(name: String): String =
    call.parameters[name] ?: badRequest("param $name required")


fun <T> tryMapOrBadRequest(value: String, map: (String) -> T): T? =
    try {
        map(value)
    } catch (e: Exception) {
        badRequest("value can not be mapped")
    }

fun badRequest(message: String): Nothing = throw throw BadRequestException(message)


val DATE_TIME_FORMAT = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd")

class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter?, value: LocalDate?) {
        out?.value(if (value == null) "null" else DATE_TIME_FORMAT.print(value))
    }

    override fun read(`in`: JsonReader?): LocalDate {
        TODO("Not yet implemented")
    }
}



data class NumberInterval(val start: Int, val end: Int) {
    fun elements(): List<Int> = (start..end).distinct()
}

private val intervalRegex = Regex("(?<start>\\d+)(-(?<end>\\d+))?")

fun toInterval(string: String): NumberInterval {
    val found = intervalRegex.find(string)
    if (found != null) {
        val groups = found.groups as MatchNamedGroupCollection
        val start = groups["start"]!!.value.toInt()
        val end = groups["end"]?.value?.toInt() ?: start
        return NumberInterval(start, end)
    }
    throw IllegalArgumentException()
}

fun toIntervalList(string: String): List<NumberInterval> {
    return string.split(',').map { it.trim() }.map { toInterval(it) }
}
