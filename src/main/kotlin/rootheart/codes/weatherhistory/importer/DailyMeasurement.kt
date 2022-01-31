package rootheart.codes.weatherhistory.importer

import org.joda.time.LocalDateTime
import java.math.BigDecimal

data class DailyMeasurement(
    val measurementTime: LocalDateTime,
    val minAirTemperature: BigDecimal? = null,
    val avgAirTemperature: BigDecimal? = null,
    val maxAirTemperature: BigDecimal? = null,
)