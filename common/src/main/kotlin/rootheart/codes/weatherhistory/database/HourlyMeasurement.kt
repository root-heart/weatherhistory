package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty1

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

data class HourlyMeasurement(
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
    var windDirectionDegrees: Int? = null,
    var visibilityInMeters: Int? = null
) {
    val precipitationTypeName get() = precipitationType?.name
    val stationIdLong get() = station.id
}

object HourlyMeasurementDao {
    fun findByStationIdAndYear(station: Station, year: Int): List<HourlyMeasurement> = transaction {
        val start = DateTime(year, 1, 1, 0, 0)
        val end = DateTime(year + 1, 1, 1, 0, 0)
        HourlyMeasurementsTable.select {
            HourlyMeasurementsTable.stationId.eq(station.id!!)
                .and(HourlyMeasurementsTable.measurementTime.greaterEq(start))
                .and(HourlyMeasurementsTable.measurementTime.less(end))
        }
            .map { toHourlyMeasurement(station, it) }
    }

    private fun toHourlyMeasurement(station: Station, row: ResultRow): HourlyMeasurement {
        val hourlyMeasurement = createHourlyMeasurement(station, row)
        setValuesFromResultRow(row, hourlyMeasurement)
        return hourlyMeasurement
    }

    private fun createHourlyMeasurement(station: Station, row: ResultRow): HourlyMeasurement {
        return HourlyMeasurement(station = station, measurementTime = row[HourlyMeasurementsTable.measurementTime])
    }

    private fun setValuesFromResultRow(row: ResultRow, hourlyMeasurement: HourlyMeasurement) {
        for (mapping in HourlyMeasurementTableMapping.mappings) {
            val property = mapping.first
            if (property is KMutableProperty1) {
                property.set(hourlyMeasurement, row[mapping.second])
            }
        }
    }
}