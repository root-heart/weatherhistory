package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.database.daily.DailyMinMax
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCalculateMinMaxWindDirections {

    private val north = BigDecimal("360")
    private val northEast = BigDecimal("45")

    private val southWest = BigDecimal("225")
    private val west = BigDecimal("270")
    private val northWest = BigDecimal("315")

    private val invalidDirection = BigDecimal("990")

    @Test
    fun testWindDirectionMinIsSmallerThanMaxIfWindIsNotComingFromNorthDirection() {
        val windDirections = DailyMinMax()
        windDirections.details = arrayOf(southWest, northWest, west, invalidDirection)
        calculateMinAndMaxWindDirection(windDirections)

        assertEquals(windDirections.min, southWest)
        assertEquals(windDirections.max, northWest)
    }


    @Test
    fun testMinIsInNorthwesternAndMaxInNorthEasternDirectionIfWindIsComingFromAroundNorth() {
        val windDirections = DailyMinMax()
        windDirections.details = arrayOf(northEast, north, northWest, invalidDirection)
        calculateMinAndMaxWindDirection(windDirections)

        assertEquals(windDirections.min, northWest)
        assertEquals(windDirections.max, northEast)
    }
}