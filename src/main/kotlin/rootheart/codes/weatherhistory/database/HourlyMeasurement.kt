package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.joda.time.DateTime
import rootheart.codes.weatherhistory.model.PrecipitationType
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal

object HourlyMeasurementsTable : LongIdTable("HOURLY_MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable.id).index("FK_IDX_HOURLY_MEASUREMENT_STATION")
    val measurementTime = datetime("MEASUREMENT_TIME")
    val airTemperatureAtTwoMetersHeightCentigrade = decimal("AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val relativeHumidityPercent = decimal("RELATIVE_HUMIDITY_PERCENT", 4, 1).nullable()
    val cloudCoverage = integer("CLOUD_COVERAGE").nullable()
    val dewPointTemperatureCentigrade = decimal("DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val airPressureHectopascals = decimal("AIR_PRESSURE_HECTOPASCALS", 5, 1).nullable()
    val precipitationMillimeters = decimal("PRECIPITATION_MILLIMETERS", 4, 1).nullable()
    val precipitationType = varchar("PRECIPITATION_TYPE", 30).nullable()
    val sunshineDurationMinutes = decimal("SUNSHINE_DURATION_MINUTES", 4, 1).nullable()
    val windSpeedMetersPerSecond = decimal("WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val windDirectionDegrees = integer("WIND_DIRECTION_DEGREES").nullable()
    val visibilityInMeters = integer("VISIBILITY_METERS").nullable()

    init {
        index(isUnique = true, stationId, measurementTime)
    }
}

object HourlyMeasurementTableMapping : TableMapping<HourlyMeasurement>(
    HourlyMeasurement::stationIdLong to HourlyMeasurementsTable.stationId,
    HourlyMeasurement::measurementTime to HourlyMeasurementsTable.measurementTime,
    HourlyMeasurement::airTemperatureAtTwoMetersHeightCentigrade to HourlyMeasurementsTable.airTemperatureAtTwoMetersHeightCentigrade,
    HourlyMeasurement::relativeHumidityPercent to HourlyMeasurementsTable.relativeHumidityPercent,
    HourlyMeasurement::cloudCoverage to HourlyMeasurementsTable.cloudCoverage,
    HourlyMeasurement::dewPointTemperatureCentigrade to HourlyMeasurementsTable.dewPointTemperatureCentigrade,
    HourlyMeasurement::airPressureHectopascals to HourlyMeasurementsTable.airPressureHectopascals,
    HourlyMeasurement::precipitationMillimeters to HourlyMeasurementsTable.precipitationMillimeters,
    HourlyMeasurement::precipitationTypeName to HourlyMeasurementsTable.precipitationType,
    HourlyMeasurement::sunshineDurationMinutes to HourlyMeasurementsTable.sunshineDurationMinutes,
    HourlyMeasurement::windSpeedMetersPerSecond to HourlyMeasurementsTable.windSpeedMetersPerSecond,
    HourlyMeasurement::maxWindSpeedMetersPerSecond to HourlyMeasurementsTable.maxWindSpeedMetersPerSecond,
    HourlyMeasurement::windDirectionDegrees to HourlyMeasurementsTable.windDirectionDegrees,
    HourlyMeasurement::visibilityInMeters to HourlyMeasurementsTable.visibilityInMeters,
)

class HourlyMeasurement(
    val id: Long? = null,
    val station: Station,
    val measurementTime: DateTime,
    var airTemperatureAtTwoMetersHeightCentigrade: BigDecimal? = null,
    var relativeHumidityPercent: BigDecimal? = null,
    var cloudCoverage: Int? = null,
    var dewPointTemperatureCentigrade: BigDecimal? = null,
    var airPressureHectopascals: BigDecimal? = null,
    var precipitationMillimeters: BigDecimal? = null,
    var precipitationType: PrecipitationType? = null,
    var sunshineDurationMinutes: BigDecimal? = null,
    var windSpeedMetersPerSecond: BigDecimal? = null,
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var windDirectionDegrees: BigDecimal? = null,
    var visibilityInMeters: BigDecimal? = null
) {
    val precipitationTypeName get() = precipitationType?.name
    val stationIdLong get() = station.id
}


