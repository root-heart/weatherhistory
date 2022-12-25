package rootheart.codes.common

import org.junit.jupiter.api.Test
//import rootheart.codes.common.collections.avgDecimal
import java.math.BigDecimal
import kotlin.test.assertEquals

class CollectionsTest {


//    @Test
//    fun testAvgDecimal() {
//        val values = listOf(
//            ValueClass(BigDecimal(20)),
//            ValueClass(BigDecimal(10)),
//            ValueClass(null)
//        )
//
//        val avg = values.avgDecimal { it.value }
//        assertEquals(BigDecimal(15), avg)
//    }
}

private data class ValueClass(var value: BigDecimal?)

