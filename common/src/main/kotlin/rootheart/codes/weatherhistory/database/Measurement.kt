package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.joda.time.LocalDate
import java.math.BigDecimal

object MeasurementsTable : LongIdTable("MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val day = date("DAY")

    val hourlyAirTemperatureCentigrade = decimalArray("HOURLY_AIR_TEMPERATURE_CENTIGRADE")
    val minAirTemperatureCentigrade = decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgAirTemperatureCentigrade = decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxAirTemperatureCentigrade = decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val hourlyDewPointTemperatureCentigrade = decimalArray("HOURLY_DEW_POINT_TEMPERATURE_CENTIGRADE")
    val minDewPointTemperatureCentigrade = decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgDewPointTemperatureCentigrade = decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxDewPointTemperatureCentigrade = decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()

    val hourlyHumidityPercent = decimalArray("HOURLY_HUMIDITY_PERCENT")
    val minHumidityPercent = decimal("MIN_HUMIDITY_PERCENT", 4, 1).nullable()
    val avgHumidityPercent = decimal("AVG_HUMIDITY_PERCENT", 4, 1).nullable()
    val maxHumidityPercent = decimal("MAX_HUMIDITY_PERCENT", 4, 1).nullable()

    val hourlyAirPressureHectopascals = decimalArray("HOURLY_AIR_PRESSURE_HECTOPASCALS")
    val minAirPressureHectopascals = decimal("MIN_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val avgAirPressureHectopascals = decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val maxAirPressureHectopascals = decimal("MAX_AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()

    val hourlyCloudCoverage = intArray("HOURLY_CLOUD_COVERAGE")

    val hourlySunshineDurationMinutes = intArray("HOURLY_SUNSHINE_DURATION_MINUTES")
    val sumSunshineDurationHours = decimal("SUM_SUNSHINE_DURATION_HOURS", 8, 1).nullable()

    val hourlyRainfallMillimeters = decimalArray("HOURLY_RAINFALL_MILLIMETERS")
    val sumRainfallMillimeters = decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable()

    val hourlySnowfallMillimeters = decimalArray("HOURLY_SNOWFALL_MILLIMETERS")
    val sumSnowfallMillimeters = decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable()

    val hourlyWindSpeedMetersPerSecond = decimalArray("HOURLY_WIND_SPEED_METERS_PER_SECOND")
    val hourlyWindDirectionDegrees = intArray("HOURLY_WIND_DIRECTION_DEGREES")
    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val avgWindSpeedMetersPerSecond = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()

    val hourlyVisibilityMeters = intArray("HOURLY_VISIBILITY_METERS")

    val minVisibilityMeters = integer("MIN_VISIBILITY_METERS").nullable()
    val avgVisibilityMeters = integer("AVG_VISIBILITY_METERS").nullable()
    val maxVisibilityMeters = integer("MAX_VISIBILITY_METERS").nullable()

    init {
        index(isUnique = true, stationId, day)
    }

    fun byStationIdAndDateBetween(stationId: Long, start: LocalDate, end: LocalDate) =
        MeasurementsTable.stationId.eq(stationId)
            .and(day.between(start.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay()))
}


object MeasurementTableMapping : TableMapping<Measurement>(
    Measurement::stationId to MeasurementsTable.stationId,
    Measurement::dayDateTime to MeasurementsTable.day,

    Measurement::hourlyAirTemperatureCentigrade to MeasurementsTable.hourlyAirTemperatureCentigrade,
    Measurement::minAirTemperatureCentigrade to MeasurementsTable.minAirTemperatureCentigrade,
    Measurement::avgAirTemperatureCentigrade to MeasurementsTable.avgAirTemperatureCentigrade,
    Measurement::maxAirTemperatureCentigrade to MeasurementsTable.maxAirTemperatureCentigrade,

    Measurement::hourlyDewPointTemperatureCentigrade to MeasurementsTable.hourlyDewPointTemperatureCentigrade,
    Measurement::minDewPointTemperatureCentigrade to MeasurementsTable.minDewPointTemperatureCentigrade,
    Measurement::avgDewPointTemperatureCentigrade to MeasurementsTable.avgDewPointTemperatureCentigrade,
    Measurement::maxDewPointTemperatureCentigrade to MeasurementsTable.maxDewPointTemperatureCentigrade,

    Measurement::hourlyHumidityPercent to MeasurementsTable.hourlyHumidityPercent,
    Measurement::minHumidityPercent to MeasurementsTable.minHumidityPercent,
    Measurement::avgHumidityPercent to MeasurementsTable.avgHumidityPercent,
    Measurement::maxHumidityPercent to MeasurementsTable.maxHumidityPercent,

    Measurement::hourlyAirPressureHectopascals to MeasurementsTable.hourlyAirPressureHectopascals,
    Measurement::minAirPressureHectopascals to MeasurementsTable.minAirPressureHectopascals,
    Measurement::avgAirPressureHectopascals to MeasurementsTable.avgAirPressureHectopascals,
    Measurement::maxAirPressureHectopascals to MeasurementsTable.maxAirPressureHectopascals,

    Measurement::hourlyCloudCoverages to MeasurementsTable.hourlyCloudCoverage,

    Measurement::hourlySunshineDurationMinutes to MeasurementsTable.hourlySunshineDurationMinutes,
    Measurement::sumSunshineDurationHours to MeasurementsTable.sumSunshineDurationHours,

    Measurement::hourlyRainfallMillimeters to MeasurementsTable.hourlyRainfallMillimeters,
    Measurement::sumRainfallMillimeters to MeasurementsTable.sumRainfallMillimeters,

    Measurement::hourlySnowfallMillimeters to MeasurementsTable.hourlySnowfallMillimeters,
    Measurement::sumSnowfallMillimeters to MeasurementsTable.sumSnowfallMillimeters,

    Measurement::hourlyWindSpeedMetersPerSecond to MeasurementsTable.hourlyWindSpeedMetersPerSecond,
    Measurement::hourlyWindDirectionDegrees to MeasurementsTable.hourlyWindDirectionDegrees,
    Measurement::maxWindSpeedMetersPerSecond to MeasurementsTable.maxWindSpeedMetersPerSecond,
    Measurement::avgWindSpeedMetersPerSecond to MeasurementsTable.avgWindSpeedMetersPerSecond,

    Measurement::hourlyVisibilityMeters to MeasurementsTable.hourlyVisibilityMeters,
    Measurement::minVisibilityMeters to MeasurementsTable.minVisibilityMeters,
    Measurement::avgVisibilityMeters to MeasurementsTable.avgVisibilityMeters,
    Measurement::maxVisibilityMeters to MeasurementsTable.maxVisibilityMeters,
)

class Measurement(
    @Transient var station: Station,
    @Transient var day: LocalDate,

    var hourlyAirTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,

    var hourlyDewPointTemperatureCentigrade: Array<BigDecimal?> = Array(24) { null },
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,

    var hourlyHumidityPercent: Array<BigDecimal?> = Array(24) { null },
    var minHumidityPercent: BigDecimal? = null,
    var maxHumidityPercent: BigDecimal? = null,
    var avgHumidityPercent: BigDecimal? = null,

    var hourlyAirPressureHectopascals: Array<BigDecimal?> = Array(24) { null },
    var minAirPressureHectopascals: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var maxAirPressureHectopascals: BigDecimal? = null,

    var hourlyCloudCoverages: Array<Int?> = Array(24) { null },

    var hourlySunshineDurationMinutes: Array<Int?> = Array(24) { null },
    var sumSunshineDurationHours: BigDecimal? = null,

    var hourlyRainfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumRainfallMillimeters: BigDecimal? = null,

    var hourlySnowfallMillimeters: Array<BigDecimal?> = Array(24) { null },
    var sumSnowfallMillimeters: BigDecimal? = null,

    var hourlyWindSpeedMetersPerSecond: Array<BigDecimal?> = Array(24) { null },
    var hourlyWindDirectionDegrees: Array<Int?> = Array(24) { null },
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,

    var hourlyVisibilityMeters: Array<Int?> = Array(24) { null },
    var minVisibilityMeters: Int? = null,
    var avgVisibilityMeters: Int? = null,
    var maxVisibilityMeters: Int? = null,
) {
    val stationId get() = station.id
    val dayDateTime get() = day.toDateTimeAtStartOfDay()!!
    val dayFormatted get() = day.toString("yyyy-MM-dd")
}
