import rootheart.codes.common.measureAndLogDuration
import java.math.BigDecimal
import kotlin.random.Random

fun main() {
//    sumInts()
    sumBigDecimals()
}

fun sumInts() {
    val r = Random(2000)
    repeat(100000) {
        measureAndLogDuration("summing values") {
            var sum = 0
            for (i in 0..100000) {
                sum += r.nextInt()
            }
//            println(sum)
        }
    }
}

fun sumBigDecimals() {
    val r = Random(2000)
    repeat(100000) {
        measureAndLogDuration("summing values") {
            var sum = BigDecimal(0)
            for (i in 0..100000) {
                sum += BigDecimal(r.nextInt())
            }
//            println(sum)
        }
    }
}