package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.joda.time.LocalDate
import java.math.BigDecimal

enum class Interval {
    YEAR, MONTH, DAY
}

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val firstDay = date("FIRST_DAY")
    val interval = enumerationByName("INTERVAL", 5, Interval::class)

    val detailedAirTemperatureCentigrade = decimalArray("DETAILED_AIR_TEMPERATURE_CENTIGRADE")
    val minAirTemperatureCentigrade = decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgAirTemperatureCentigrade = decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxAirTemperatureCentigrade = decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val detailedDewPointTemperatureCentigrade = decimalArray("DETAILED_DEW_POINT_TEMPERATURE_CENTIGRADE")
    val minDewPointTemperatureCentigrade = decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgDewPointTemperatureCentigrade = decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxDewPointTemperatureCentigrade = decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val detailedHumidityPercent = decimalArray("DETAILED_HUMIDITY_PERCENT")
    val minHumidityPercent = decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable()
    val avgHumidityPercent = decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable()
    val maxHumidityPercent = decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable()

    val detailedAirPressureHectopascals = decimalArray("DETAILED_AIR_PRESSURE_HECTOPASCALS")
    val minAirPressureHectopascals = decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val avgAirPressureHectopascals = decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val maxAirPressureHectopascals = decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()

    val detailedCloudCoverage = intArray("DETAILED_CLOUD_COVERAGE")
    val cloudCoverageHistogram = intArray("CLOUD_COVERAGE_HISTOGRAM")

    val detailedSunshineDurationMinutes = intArray("DETAILED_SUNSHINE_DURATION_MINUTES")
    val sumSunshineDurationHours = decimal("SUM_SUNSHINE_DURATION_HOURS", 8, 1).nullable()

    val detailedRainfallMillimeters = decimalArray("DETAILED_RAINFALL_MILLIMETERS")
    val sumRainfallMillimeters = decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable()

    val detailedSnowfallMillimeters = decimalArray("DETAILED_SNOWFALL_MILLIMETERS")
    val sumSnowfallMillimeters = decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable()

    val detailedWindSpeedMetersPerSecond = decimalArray("DETAILED_WIND_SPEED_METERS_PER_SECOND")
    val detailedWindDirectionDegrees = intArray("DETAILED_WIND_DIRECTION_DEGREES")
    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val avgWindSpeedMetersPerSecond = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()

    val detailedVisibilityMeters = intArray("DETAILED_VISIBILITY_METERS")

    val minVisibilityMeters = integer("MIN_VISIBILITY_METERS").nullable()
    val avgVisibilityMeters = integer("AVG_VISIBILITY_METERS").nullable()
    val maxVisibilityMeters = integer("MAX_VISIBILITY_METERS").nullable()

    init {
        index(isUnique = true, stationId, firstDay, interval)
    }
}


object MeasurementTableMapping : TableMapping<Measurement>(
    Measurement::stationId to MeasurementsTable.stationId,
    Measurement::firstDayDateTime to MeasurementsTable.firstDay,
    Measurement::interval to MeasurementsTable.interval,

    Measurement::detailedAirTemperatureCentigrade to MeasurementsTable.detailedAirTemperatureCentigrade,
    Measurement::minAirTemperatureCentigrade to MeasurementsTable.minAirTemperatureCentigrade,
    Measurement::avgAirTemperatureCentigrade to MeasurementsTable.avgAirTemperatureCentigrade,
    Measurement::maxAirTemperatureCentigrade to MeasurementsTable.maxAirTemperatureCentigrade,

    Measurement::detailedDewPointTemperatureCentigrade to MeasurementsTable.detailedDewPointTemperatureCentigrade,
    Measurement::minDewPointTemperatureCentigrade to MeasurementsTable.minDewPointTemperatureCentigrade,
    Measurement::avgDewPointTemperatureCentigrade to MeasurementsTable.avgDewPointTemperatureCentigrade,
    Measurement::maxDewPointTemperatureCentigrade to MeasurementsTable.maxDewPointTemperatureCentigrade,

    Measurement::detailedHumidityPercent to MeasurementsTable.detailedHumidityPercent,
    Measurement::minHumidityPercent to MeasurementsTable.minHumidityPercent,
    Measurement::avgHumidityPercent to MeasurementsTable.avgHumidityPercent,
    Measurement::maxHumidityPercent to MeasurementsTable.maxHumidityPercent,

    Measurement::detailedAirPressureHectopascals to MeasurementsTable.detailedAirPressureHectopascals,
    Measurement::minAirPressureHectopascals to MeasurementsTable.minAirPressureHectopascals,
    Measurement::avgAirPressureHectopascals to MeasurementsTable.avgAirPressureHectopascals,
    Measurement::maxAirPressureHectopascals to MeasurementsTable.maxAirPressureHectopascals,

    Measurement::detailedCloudCoverages to MeasurementsTable.detailedCloudCoverage,
    Measurement::cloudCoverageHistogram to MeasurementsTable.cloudCoverageHistogram,

    Measurement::detailedSunshineDurationHours to MeasurementsTable.detailedSunshineDurationMinutes,
    Measurement::sumSunshineDurationHours to MeasurementsTable.sumSunshineDurationHours,

    Measurement::detailedRainfallMillimeters to MeasurementsTable.detailedRainfallMillimeters,
    Measurement::sumRainfallMillimeters to MeasurementsTable.sumRainfallMillimeters,

    Measurement::detailedSnowfallMillimeters to MeasurementsTable.detailedSnowfallMillimeters,
    Measurement::sumSnowfallMillimeters to MeasurementsTable.sumSnowfallMillimeters,

    Measurement::detailedWindSpeedMetersPerSecond to MeasurementsTable.detailedWindSpeedMetersPerSecond,
    Measurement::detailedWindDirectionDegrees to MeasurementsTable.detailedWindDirectionDegrees,
    Measurement::maxWindSpeedMetersPerSecond to MeasurementsTable.maxWindSpeedMetersPerSecond,
    Measurement::avgWindSpeedMetersPerSecond to MeasurementsTable.avgWindSpeedMetersPerSecond,

    Measurement::detailedVisibilityMeters to MeasurementsTable.detailedVisibilityMeters,
    Measurement::minVisibilityMeters to MeasurementsTable.minVisibilityMeters,
    Measurement::avgVisibilityMeters to MeasurementsTable.avgVisibilityMeters,
    Measurement::maxVisibilityMeters to MeasurementsTable.maxVisibilityMeters,
)

class Measurement(
    @Transient var station: Station,
    @Transient var firstDay: LocalDate,
    val interval: Interval,

    val detailedAirTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,

    val detailedDewPointTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,

    val detailedHumidityPercent: Array<BigDecimal?> = Array(24) { null },
    var minHumidityPercent: BigDecimal? = null,
    var maxHumidityPercent: BigDecimal? = null,
    var avgHumidityPercent: BigDecimal? = null,

    val detailedAirPressureHectopascals: Array<BigDecimal?> = Array(24) { null },
    var minAirPressureHectopascals: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var maxAirPressureHectopascals: BigDecimal? = null,

    val detailedCloudCoverages: Array<Int?> = Array(24) { null },
    var cloudCoverageHistogram: Array<Int> = Array(10) { 0 },

    val detailedSunshineDurationHours: Array<BigDecimal?> = Array(24) { null },
    var sumSunshineDurationHours: BigDecimal? = null,

    val detailedRainfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumRainfallMillimeters: BigDecimal? = null,

    val detailedSnowfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumSnowfallMillimeters: BigDecimal? = null,

    val detailedWindSpeedMetersPerSecond: Array<BigDecimal?> = Array(24) { null },
    val detailedWindDirectionDegrees: Array<Int?> = Array(24) { null },
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,

    val detailedVisibilityMeters: Array<Int?> = Array(24) { null },
    var minVisibilityMeters: Int? = null,
    var avgVisibilityMeters: Int? = null,
    var maxVisibilityMeters: Int? = null,
) {
    val stationId get() = station.id
    val firstDayDateTime get() = firstDay.toDateTimeAtStartOfDay()!!
    val dayFormatted get() = firstDay.toString("yyyy-MM-dd")
}
