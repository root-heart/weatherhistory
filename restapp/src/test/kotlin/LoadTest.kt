//import rootheart.codes.weatherhistory.restapp.resources.stations.measurementTypeColumnsMapping
import java.net.URL
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main() {
//    val server = "192.168.178.51"
//    val server = "localhost"
//    val port = "8080"
//    val stationIds = 1..2
//    val measurementTypes = measurementTypeColumnsMapping.keys
//    val years = 1990..1991
//    val months = 1..12
//    val days = 10..20
//    val resolutions = listOf("monthly", "daily")
//    val urls = stationIds.map { "http://$server:$port/stations/$it" }
//        .flatMap { urlPart -> measurementTypes.map { "$urlPart/$it" } }
//        .flatMap { urlPart -> years.map { "$urlPart/$it" } }
//        .flatMap { urlPart -> months.map { "$urlPart/$it" } }
//        .flatMap { urlPart -> days.map { "$urlPart/$it" } }
//        .flatMap { urlPart -> resolutions.map { "$urlPart?resolution=$it" } }
//    .map(::URL)

//    responseTimeTest(urls)
//    throughputTest(urls)
}

// JDBC + ktor 2.1.3      min 308   max 1647   avg 436   med 567
// Exposed + ktor 2.1.3   min 524   max 1862   avg 676   med 750
@OptIn(ExperimentalTime::class)
fun responseTimeTest(urls: List<URL>) {
    repeat(10) {
        val durations = urls.map { measureTime { it.readBytes() }.inWholeMicroseconds }
        println("Run $it:   ${durations.getSummaryString()}")

    }
}

fun throughputTest(urls: List<URL>) {
    println("Running throughput test")

    // JDBC + Ktor 1.6.8       min  9084   max 10238   avg  9861   med  9591
    // Exposed + Ktor 1.6.8    min  5870   max  7413   avg  6975   med  5870
    // JDBC + Ktor 2.1.3       min  9347   max 10278   avg  9873   med 10238
    // Exposed + Ktor 2.1.3    min  6409   max  7456   avg  7083   med  7156
    val requestsPerSecondList = ArrayList<Long>()
    repeat(100) { run ->
        print("Run $run: ")
        val start = System.currentTimeMillis()
        urls.parallelStream().forEach(URL::readBytes)
        val current = System.currentTimeMillis()
        val duration = current - start
        val requestsPerSecond = urls.size * 1000 / duration
        if (run > 20) {
            requestsPerSecondList.add(requestsPerSecond)
        }
        println("${urls.size} requests in $duration millis => $requestsPerSecond req/s")
    }


    println(requestsPerSecondList.getSummaryString())
}

private fun List<Long>.getSummaryString(): String =
    "min ${minOrNull()}   max ${maxOrNull()}   avg ${sum() / size}   med ${this[size / 2]}"

