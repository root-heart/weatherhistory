package rootheart.codes.common

import org.junit.jupiter.api.Test
import rootheart.codes.common.collections.nullsafeAvgDecimal
import rootheart.codes.common.collections.nullsafeAvgDecimals
//import rootheart.codes.common.collections.avgDecimal
import java.math.BigDecimal
import kotlin.test.assertEquals

class CollectionsTest {


    @Test
    fun testAvgDecimal() {
        val values = listOf(76.0, 81.0, 75.0, 77.0, 77.0, 75.0, 82.0, 94.0, 96.0, 94.0, 94.0, 94.0, 93.0, 93.0, 94.0,
                            97.0, 99.0, 97.0, 88.0, 98.0, 100.0, 98.0, 98.0, 100.0).map(::BigDecimal)

        val avg = values.nullsafeAvgDecimal { it }
        assertEquals(BigDecimal(90), avg)
    }
}

private data class ValueClass(var value: BigDecimal?)

