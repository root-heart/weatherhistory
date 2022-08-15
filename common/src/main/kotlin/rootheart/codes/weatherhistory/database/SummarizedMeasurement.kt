package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty1

object SummarizedMeasurementsTable : LongIdTable("SUMMARIZED_MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable).index("FK_IDX_MEASUREMENT_STATION")
    val firstDay = date("FIRST_DAY")
    val lastDay = date("LAST_DAY")
    val intervalType = varchar("INTERVAL_TYPE", 6)

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

    val countCloudCoverage0 = integer("COUNT_CLOUD_COVERAGE0").nullable()
    val countCloudCoverage1 = integer("COUNT_CLOUD_COVERAGE1").nullable()
    val countCloudCoverage2 = integer("COUNT_CLOUD_COVERAGE2").nullable()
    val countCloudCoverage3 = integer("COUNT_CLOUD_COVERAGE3").nullable()
    val countCloudCoverage4 = integer("COUNT_CLOUD_COVERAGE4").nullable()
    val countCloudCoverage5 = integer("COUNT_CLOUD_COVERAGE5").nullable()
    val countCloudCoverage6 = integer("COUNT_CLOUD_COVERAGE6").nullable()
    val countCloudCoverage7 = integer("COUNT_CLOUD_COVERAGE7").nullable()
    val countCloudCoverage8 = integer("COUNT_CLOUD_COVERAGE8").nullable()
    val countCloudCoverageNotVisible = integer("COUNT_CLOUD_COVERAGE_NOT_VISIBLE").nullable()
    val countCloudCoverageNotMeasured = integer("COUNT_CLOUD_COVERAGE_NOT_MEASURED").nullable()

    val sumSunshineDurationHours = decimal("SUM_SUNSHINE_DURATION_HOURS", 8, 1).nullable()

    val sumRainfallMillimeters = decimal("SUM_RAINFALL_MILLIMETERS", 6, 1).nullable()
    val sumSnowfallMillimeters = decimal("SUM_SNOWFALL_MILLIMETERS", 6, 1).nullable()

    val maxWindSpeedMetersPerSecond = decimal("MAX_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()
    val avgWindSpeedMetersPerSecond = decimal("AVG_WIND_SPEED_METERS_PER_SECOND", 4, 1).nullable()

    init {
        index(isUnique = true, stationId, firstDay, lastDay)
    }
}

object SummarizedMeasurementTableMapping : TableMapping<SummarizedMeasurement>(
    SummarizedMeasurement::stationIdLong to SummarizedMeasurementsTable.stationId,
    SummarizedMeasurement::firstDay to SummarizedMeasurementsTable.firstDay,
    SummarizedMeasurement::lastDay to SummarizedMeasurementsTable.lastDay,
    SummarizedMeasurement::intervalTypeName to SummarizedMeasurementsTable.intervalType,

    SummarizedMeasurement::countCloudCoverage0 to SummarizedMeasurementsTable.countCloudCoverage0,
    SummarizedMeasurement::countCloudCoverage1 to SummarizedMeasurementsTable.countCloudCoverage1,
    SummarizedMeasurement::countCloudCoverage2 to SummarizedMeasurementsTable.countCloudCoverage2,
    SummarizedMeasurement::countCloudCoverage3 to SummarizedMeasurementsTable.countCloudCoverage3,
    SummarizedMeasurement::countCloudCoverage4 to SummarizedMeasurementsTable.countCloudCoverage4,
    SummarizedMeasurement::countCloudCoverage5 to SummarizedMeasurementsTable.countCloudCoverage5,
    SummarizedMeasurement::countCloudCoverage6 to SummarizedMeasurementsTable.countCloudCoverage6,
    SummarizedMeasurement::countCloudCoverage7 to SummarizedMeasurementsTable.countCloudCoverage7,
    SummarizedMeasurement::countCloudCoverage8 to SummarizedMeasurementsTable.countCloudCoverage8,
    SummarizedMeasurement::countCloudCoverageNotMeasured to SummarizedMeasurementsTable.countCloudCoverageNotMeasured,
    SummarizedMeasurement::countCloudCoverageNotVisible to SummarizedMeasurementsTable.countCloudCoverageNotVisible,

    SummarizedMeasurement::minDewPointTemperatureCentigrade to SummarizedMeasurementsTable.minDewPointTemperatureCentigrade,
    SummarizedMeasurement::avgDewPointTemperatureCentigrade to SummarizedMeasurementsTable.avgDewPointTemperatureCentigrade,
    SummarizedMeasurement::maxDewPointTemperatureCentigrade to SummarizedMeasurementsTable.maxDewPointTemperatureCentigrade,

    SummarizedMeasurement::minHumidityPercent to SummarizedMeasurementsTable.minHumidityPercent,
    SummarizedMeasurement::avgHumidityPercent to SummarizedMeasurementsTable.avgHumidityPercent,
    SummarizedMeasurement::maxHumidityPercent to SummarizedMeasurementsTable.maxHumidityPercent,

    SummarizedMeasurement::minAirTemperatureCentigrade to SummarizedMeasurementsTable.minAirTemperatureCentigrade,
    SummarizedMeasurement::avgAirTemperatureCentigrade to SummarizedMeasurementsTable.avgAirTemperatureCentigrade,
    SummarizedMeasurement::maxAirTemperatureCentigrade to SummarizedMeasurementsTable.maxAirTemperatureCentigrade,

    SummarizedMeasurement::sumSunshineDurationHours to SummarizedMeasurementsTable.sumSunshineDurationHours,
    SummarizedMeasurement::sumRainfallMillimeters to SummarizedMeasurementsTable.sumRainfallMillimeters,
    SummarizedMeasurement::sumSnowfallMillimeters to SummarizedMeasurementsTable.sumSnowfallMillimeters,

    SummarizedMeasurement::maxWindSpeedMetersPerSecond to SummarizedMeasurementsTable.maxWindSpeedMetersPerSecond,
    SummarizedMeasurement::avgWindSpeedMetersPerSecond to SummarizedMeasurementsTable.avgWindSpeedMetersPerSecond,

    SummarizedMeasurement::minAirPressureHectopascals to SummarizedMeasurementsTable.minAirPressureHectopascals,
    SummarizedMeasurement::avgAirPressureHectopascals to SummarizedMeasurementsTable.avgAirPressureHectopascals,
    SummarizedMeasurement::maxAirPressureHectopascals to SummarizedMeasurementsTable.maxAirPressureHectopascals,
)

class SummarizedMeasurement(
    val station: Station,
    val interval: DateInterval,
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,
    var minHumidityPercent: BigDecimal? = null,
    var maxHumidityPercent: BigDecimal? = null,
    var avgHumidityPercent: BigDecimal? = null,
    var countCloudCoverage0: Int = 0,
    var countCloudCoverage1: Int = 0,
    var countCloudCoverage2: Int = 0,
    var countCloudCoverage3: Int = 0,
    var countCloudCoverage4: Int = 0,
    var countCloudCoverage5: Int = 0,
    var countCloudCoverage6: Int = 0,
    var countCloudCoverage7: Int = 0,
    var countCloudCoverage8: Int = 0,
    var countCloudCoverageNotVisible: Int = 0,
    var countCloudCoverageNotMeasured: Int = 0,
    var sumSunshineDurationHours: BigDecimal? = null,
    var sumRainfallMillimeters: BigDecimal? = null,
    var sumSnowfallMillimeters: BigDecimal? = null,
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,
    var minAirPressureHectopascals: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
    var maxAirPressureHectopascals: BigDecimal? = null,
    var details: String? = null
) {
    val stationIdLong get() = station.id
    val firstDay get() = interval.firstDay
    val firstDayMillis get() = interval.firstDay.millis
    val lastDay get() = interval.lastDay
    val lastDayMillis get() = interval.lastDay.millis
    val intervalTypeName get() = interval.type.name
}

object SummarizedMeasurementDao {
    fun findByStationIdAndDateBetween(
        station: Station,
        from: DateTime,
        to: DateTime,
        intervalType: DateIntervalType
    ): List<SummarizedMeasurement> = transaction {
        SummarizedMeasurementsTable.select {
            SummarizedMeasurementsTable.stationId.eq(station.id!!)
                .and(SummarizedMeasurementsTable.intervalType eq intervalType.name)
                .and(SummarizedMeasurementsTable.firstDay greaterEq from)
                .and(SummarizedMeasurementsTable.firstDay less to)
        }
            .orderBy(SummarizedMeasurementsTable.firstDay)
            .map { toSummarizedMeasurement(station, it) }
    }

    private fun toSummarizedMeasurement(station: Station, row: ResultRow): SummarizedMeasurement {
        val summarizedMeasurement = createSummarizedMeasurement(station, row)
        setValuesFromResultRow(row, summarizedMeasurement)
        return summarizedMeasurement
    }

    private fun createSummarizedMeasurement(station: Station, row: ResultRow): SummarizedMeasurement {
        val interval = DateInterval(
            row[SummarizedMeasurementsTable.firstDay],
            row[SummarizedMeasurementsTable.lastDay],
            DateIntervalType.valueOf(row[SummarizedMeasurementsTable.intervalType])
        )
        return SummarizedMeasurement(station, interval)
    }

    private fun setValuesFromResultRow(row: ResultRow, summarizedMeasurement: SummarizedMeasurement) {
        for (mapping in SummarizedMeasurementTableMapping.mappings) {
            val property = mapping.first
            if (property is KMutableProperty1) {
                property.set(summarizedMeasurement, row[mapping.second])
            }
        }
    }
}

enum class DateIntervalType {
    DAY, MONTH, SEASON, YEAR, DECADE
}

data class DateInterval(
    val firstDay: DateTime,
    val lastDay: DateTime,
    val type: DateIntervalType
) {
    companion object {
        @JvmStatic
        fun day(time: DateTime): DateInterval {
            return DateInterval(
                time.withTimeAtStartOfDay(),
                time.withTimeAtStartOfDay(),
                DateIntervalType.DAY
            )
        }

        @JvmStatic
        fun month(year: Int, month: Int): DateInterval {
            val fromDate = LocalDate(year, month, 1)
            val toDate = fromDate.plusMonths(1).minusDays(1)
            return DateInterval(
                fromDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                toDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                DateIntervalType.MONTH
            )
        }

//        fun month(day: Date) = month(day.year, day.monthOfYear)

        @JvmStatic
        fun month(time: DateTime) = month(time.year, time.monthOfYear)

        @JvmStatic
        fun season(time: DateTime): DateInterval {
            val startMonth: Int
            var startYear = time.year
            when (time.monthOfYear) {
                1, 2 -> {
                    startMonth = 12
                    startYear--
                }
                3, 4, 5 -> startMonth = 3
                6, 7, 8 -> startMonth = 6
                9, 10, 11 -> startMonth = 9
                else -> startMonth = 12
            }
            val fromDate = LocalDate(startYear, startMonth, 1)
            val toDate = fromDate.plusMonths(3).minusDays(1)
            return DateInterval(
                fromDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                toDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                DateIntervalType.SEASON
            )
        }

        @JvmStatic
        fun year(time: DateTime): DateInterval {
            val fromDate = LocalDate(time.year, 1, 1)
            val toDate = fromDate.plusYears(1).minusDays(1)
            return DateInterval(
                fromDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                toDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                DateIntervalType.YEAR
            )
        }

        @JvmStatic
        fun decade(time: DateTime): DateInterval {
            val startYear = (time.year / 10) * 10
            val fromDate = LocalDate(startYear, 1, 1)
            val toDate = fromDate.plusYears(10).minusDays(1)
            return DateInterval(
                fromDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                toDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                DateIntervalType.DECADE
            )
        }
    }
}
