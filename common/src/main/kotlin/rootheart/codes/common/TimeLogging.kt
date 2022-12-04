package rootheart.codes.common

import mu.KotlinLogging
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

val log = KotlinLogging.logger { }

@OptIn(ExperimentalTime::class)
inline fun <T> Any.measureAndLogDuration(identifier: String, block: () -> T): T {
    val (result, duration) = measureTimedValue(block)
    val className = javaClass.simpleName
    log.info { "$className $identifier took $duration" }
    return result
}