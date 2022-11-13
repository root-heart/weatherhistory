package rootheart.codes.common

import mu.KotlinLogging
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
inline fun <T> measureAndLogDuration(identifier: String, block: () -> T): T {
    val log = KotlinLogging.logger { }
    val (result, duration) = measureTimedValue(block)
    log.info { "$identifier took $duration" }
    return result
}