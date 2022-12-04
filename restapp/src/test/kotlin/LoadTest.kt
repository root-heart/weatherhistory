import java.net.URL
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

val results = HashMap<String, MutableList<Duration>>()

fun main() {
    repeat(3) {
        val stationIds = listOf(1)
        val measurementTypes = listOf("temperature", "air-pressure", "dew-point-temperature", "visibility", "humidity")
        val resolutions = listOf("monthly", "daily")
        val years = 1980..2022
        val months = 1..12
        for (stationId in stationIds) {
            for (measurementType in measurementTypes) {
                for (res in resolutions) {
                    for (year in years) {
                        measure("http://localhost:8080/stations/$stationId/$measurementType/$res/$year")
                            .let { results.getOrPut(measurementType) { ArrayList() }.add(it) }
                        for (month in months) {
                            measure("http://localhost:8080/stations/$stationId/$measurementType/$res/$year/$month")
                                .let { results.getOrPut(measurementType) { ArrayList() }.add(it) }
                        }
                    }
                }
            }
        }

        println("path\tmin\tavg\tmax\tcount")
        for (result in results) {
            with(result.value.map { it.inWholeMicroseconds }) {
                println("${result.key}\t${minOrNull()}\t${sum() / size}\t${maxOrNull()}\t$size")
            }
        }

        results.clear()
    }
}


@OptIn(ExperimentalTime::class)
fun measure(urlString: String): Duration {
    val duration = measureTime {
        URL(urlString).readBytes()
    }
//    println("$urlString took $duration")
    return duration
}