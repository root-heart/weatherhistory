package rootheart.codes.weatherhistory.restapp

import java.math.BigDecimal

data class StationJson(
    val id: Long,
    val name: String,
    val federalState: String,
    val height: Int,
    val latitude: BigDecimal,
    val longitude: BigDecimal
)