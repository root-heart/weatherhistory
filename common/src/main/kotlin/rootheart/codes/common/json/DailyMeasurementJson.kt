import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.LocalDate
import rootheart.codes.common.collections.AvgMaxDetails
import rootheart.codes.common.collections.MinMaxSumDetails
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementTable
import java.math.BigDecimal


data class DailyMinAvgMaxDetailsJson<N : Number>(
        var min: N? = null,
        var avg: N? = null,
        var max: N? = null,
        var details: Array<N?>? = null
)

data class DailyMeasurementJson(
        val date: LocalDate,
        val airTemperature: DailyMinAvgMaxDetailsJson<BigDecimal> = DailyMinAvgMaxDetailsJson(),
        val dewPointTemperature: DailyMinAvgMaxDetailsJson<BigDecimal> = DailyMinAvgMaxDetailsJson(),
        val humidity: DailyMinAvgMaxDetailsJson<BigDecimal> = DailyMinAvgMaxDetailsJson(),
        val airPressure: DailyMinAvgMaxDetailsJson<BigDecimal> = DailyMinAvgMaxDetailsJson(),
        var cloudCoverage: List<Int> = emptyList(),
        val sunshine: MinMaxSumDetails<Int> = MinMaxSumDetails(),
        val rainfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val snowfall: MinMaxSumDetails<BigDecimal> = MinMaxSumDetails(),
        val windSpeed: AvgMaxDetails<BigDecimal> = AvgMaxDetails(),
        val visibility: DailyMinAvgMaxDetailsJson<Int> = DailyMinAvgMaxDetailsJson(),
)

fun DailyMeasurementTable.toJson(row: ResultRow): DailyMeasurementJson {
    val json = DailyMeasurementJson(row[date].toLocalDate())
    json.airTemperature.min = row[airTemperatureCentigrade.min]
    return json
}