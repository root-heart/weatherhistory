package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import java.math.BigDecimal

object MonthlySummaryTable : LongIdTable("MONTHLY_SUMMARY") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MONTHLY_SUMMARY_STATION")
    val year = integer("YEAR")
    val month = integer("MONTH")

    val minAirTemperatureCentigrade = decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgAirTemperatureCentigrade = decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxAirTemperatureCentigrade = decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val minDewPointTemperatureCentigrade = decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgDewPointTemperatureCentigrade = decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxDewPointTemperatureCentigrade = decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val minHumidityPercent = decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable()
    val avgHumidityPercent = decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable()
    val maxHumidityPercent = decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable()

    val minAirPressureHectopascals = decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val avgAirPressureHectopascals = decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val maxAirPressureHectopascals = decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()

    val cloudCoverageHistogram = intArray("CLOUD_COVERAGE_HISTOGRAM")

    val sumSunshineDurationHours = decimal("SUM_SUNSHINE_DURATION_HOURS", 8, 1).nullable()

    val sumRainfallMillimeters = decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable()

    val sumSnowfallMillimeters = decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable()

    val avgWindSpeedMetersPerSecond = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 6, 1).nullable()
    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 6, 1).nullable()

    val minVisibilityMeters = integer("MIN_VISIBILITY_METERS").nullable()
    val avgVisibilityMeters = integer("AVG_VISIBILITY_METERS").nullable()
    val maxVisibilityMeters = integer("MAX_VISIBILITY_METERS").nullable()

    init {
        index(isUnique = true, stationId, year, month)
    }

}

object MonthlySummaryTableMapping : TableMapping<MonthlySummary>(
    MonthlySummary::stationId to MonthlySummaryTable.stationId,
    MonthlySummary::year to MonthlySummaryTable.year,
    MonthlySummary::month to MonthlySummaryTable.month,
    MonthlySummary::minAirTemperatureCentigrade to MonthlySummaryTable.minAirTemperatureCentigrade,
    MonthlySummary::avgAirTemperatureCentigrade to MonthlySummaryTable.avgAirTemperatureCentigrade,
    MonthlySummary::maxAirTemperatureCentigrade to MonthlySummaryTable.maxAirTemperatureCentigrade,
    MonthlySummary::minDewPointTemperatureCentigrade to MonthlySummaryTable.minDewPointTemperatureCentigrade,
    MonthlySummary::avgDewPointTemperatureCentigrade to MonthlySummaryTable.avgDewPointTemperatureCentigrade,
    MonthlySummary::maxDewPointTemperatureCentigrade to MonthlySummaryTable.maxDewPointTemperatureCentigrade,
    MonthlySummary::minHumidityPercent to MonthlySummaryTable.minHumidityPercent,
    MonthlySummary::avgHumidityPercent to MonthlySummaryTable.avgHumidityPercent,
    MonthlySummary::maxHumidityPercent to MonthlySummaryTable.maxHumidityPercent,
    MonthlySummary::minAirPressureHectopascals to MonthlySummaryTable.minAirPressureHectopascals,
    MonthlySummary::avgAirPressureHectopascals to MonthlySummaryTable.avgAirPressureHectopascals,
    MonthlySummary::maxAirPressureHectopascals to MonthlySummaryTable.maxAirPressureHectopascals,
    MonthlySummary::cloudCoverageHistogram to MonthlySummaryTable.cloudCoverageHistogram,
    MonthlySummary::sumSunshineDurationHours to MonthlySummaryTable.sumSunshineDurationHours,
    MonthlySummary::sumRainfallMillimeters to MonthlySummaryTable.sumRainfallMillimeters,
    MonthlySummary::sumSnowfallMillimeters to MonthlySummaryTable.sumSnowfallMillimeters,
    MonthlySummary::avgWindSpeedMetersPerSecond to MonthlySummaryTable.avgWindSpeedMetersPerSecond,
    MonthlySummary::maxWindSpeedMetersPerSecond to MonthlySummaryTable.maxWindSpeedMetersPerSecond,
    MonthlySummary::minVisibilityMeters to MonthlySummaryTable.minVisibilityMeters,
    MonthlySummary::avgVisibilityMeters to MonthlySummaryTable.avgVisibilityMeters,
    MonthlySummary::maxVisibilityMeters to MonthlySummaryTable.maxVisibilityMeters,
)

class MonthlySummary(
    var station: Station,
    var year: Int,
    var month: Int,

    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,

    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,

    var minHumidityPercent: BigDecimal? = null,
    var maxHumidityPercent: BigDecimal? = null,
    var avgHumidityPercent: BigDecimal? = null,

    var minAirPressureHectopascals: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var maxAirPressureHectopascals: BigDecimal? = null,

    var cloudCoverageHistogram: Array<Int> = Array(9) { 0 },

    var sumSunshineDurationHours: BigDecimal? = null,
    var sumRainfallMillimeters: BigDecimal? = null,
    var sumSnowfallMillimeters: BigDecimal? = null,

    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,

    var minVisibilityMeters: Int? = null,
    var avgVisibilityMeters: Int? = null,
    var maxVisibilityMeters: Int? = null,
) {
    val stationId get() = station.id
}