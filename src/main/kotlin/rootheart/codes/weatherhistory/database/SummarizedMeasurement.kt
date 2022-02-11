package rootheart.codes.weatherhistory.database

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import rootheart.codes.weatherhistory.model.StationId
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty1

object SummarizedMeasurementsTable : LongIdTable("SUMMARIZED_MEASUREMENTS") {
    val stationId = reference("STATION_ID", StationsTable.stationId).index("FK_IDX_MEASUREMENT_STATION")
    val firstDay = date("FIRST_DAY")
    val lastDay = date("LAST_DAY")
    val intervalType = varchar("INTERVAL_TYPE", 6)
    val minAirTemperatureCentigrade = decimal("MIN_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgAirTemperatureCentigrade = decimal("AVG_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxAirTemperatureCentigrade = decimal("MAX_AIR_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val minDewPointTemperatureCentigrade = decimal("MIN_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val avgDewPointTemperatureCentigrade = decimal("AVG_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
    val maxDewPointTemperatureCentigrade = decimal("MAX_DEW_POINT_TEMPERATURE_CENTIGRADE", 4, 1).nullable()
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
    val avgAirPressureHectopascals = decimal("AVG_AIR_PRESSURE_HECTOPASCALS", 4, 1).nullable()

    init {
        index(isUnique = true, stationId, firstDay, lastDay)
    }
}

object SummarizedMeasurementTableMapping : TableMapping<SummarizedMeasurement>(
    SummarizedMeasurement::stationIdInt to SummarizedMeasurementsTable.stationId,
    SummarizedMeasurement::firstDay to SummarizedMeasurementsTable.firstDay,
    SummarizedMeasurement::lastDay to SummarizedMeasurementsTable.lastDay,
    SummarizedMeasurement::intervalType to SummarizedMeasurementsTable.intervalType,
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
    SummarizedMeasurement::minAirTemperatureCentigrade to SummarizedMeasurementsTable.minAirTemperatureCentigrade,
    SummarizedMeasurement::avgAirTemperatureCentigrade to SummarizedMeasurementsTable.avgAirTemperatureCentigrade,
    SummarizedMeasurement::maxAirTemperatureCentigrade to SummarizedMeasurementsTable.maxAirTemperatureCentigrade,
    SummarizedMeasurement::sumSunshineDurationHours to SummarizedMeasurementsTable.sumSunshineDurationHours,
    SummarizedMeasurement::sumRainfallMillimeters to SummarizedMeasurementsTable.sumRainfallMillimeters,
    SummarizedMeasurement::sumSnowfallMillimeters to SummarizedMeasurementsTable.sumSnowfallMillimeters,
    SummarizedMeasurement::maxWindSpeedMetersPerSecond to SummarizedMeasurementsTable.maxWindSpeedMetersPerSecond,
    SummarizedMeasurement::avgWindSpeedMetersPerSecond to SummarizedMeasurementsTable.avgWindSpeedMetersPerSecond,
    SummarizedMeasurement::avgAirPressureHectopascals to SummarizedMeasurementsTable.avgAirPressureHectopascals,
)

class SummarizedMeasurement(
    val stationId: StationId,
    private val interval: DateInterval,
    var minAirTemperatureCentigrade: BigDecimal? = null,
    var avgAirTemperatureCentigrade: BigDecimal? = null,
    var maxAirTemperatureCentigrade: BigDecimal? = null,
    var minDewPointTemperatureCentigrade: BigDecimal? = null,
    var maxDewPointTemperatureCentigrade: BigDecimal? = null,
    var avgDewPointTemperatureCentigrade: BigDecimal? = null,
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
    var sumRainfallMillimeters: BigDecimal = BigDecimal.ZERO,
    var sumSnowfallMillimeters: BigDecimal = BigDecimal.ZERO,
    var maxWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgWindSpeedMetersPerSecond: BigDecimal? = null,
    var avgAirPressureHectopascals: BigDecimal? = null,
) {
    val stationIdInt get() = stationId.stationId
    val firstDay get() = interval.firstDay
    val lastDay get() = interval.lastDay
    val intervalType get() = interval.type.name
}

object SummarizedMeasurementDao {
    fun findByStationIdAndYear(stationId: Int, year: Int): List<SummarizedMeasurement> {
        return transaction {
            val start = DateTime(year, 1, 1, 0, 0)
            val end = DateTime(year + 1, 1, 1, 0, 0)
            return@transaction selectAll(stationId, start, end, DateIntervalType.MONTH)
        }
    }

    fun findByStationIdAndYearAndMonth(stationId: Int, year: Int, month: Int): List<SummarizedMeasurement> {
        return transaction {
            val start = DateTime(year, month, 1, 0, 0)
            val end = DateTime(year, month + 1, 1, 0, 0)
            return@transaction selectAll(stationId, start, end, DateIntervalType.DAY)
        }
    }

    private fun selectAll(stationId: Int, start: DateTime, end: DateTime, intervalType: DateIntervalType) =
        SummarizedMeasurementsTable.select {
            SummarizedMeasurementsTable.stationId.eq(stationId)
                .and(SummarizedMeasurementsTable.intervalType eq intervalType.name)
                .and(SummarizedMeasurementsTable.firstDay greaterEq start)
                .and(SummarizedMeasurementsTable.lastDay less end)
        }.map(::toSummarizedMeasurement)

    private fun toSummarizedMeasurement(row: ResultRow): SummarizedMeasurement {
        val summarizedMeasurement = createSummarizedMeasurement(row)
        setValuesFromResultRow(row, summarizedMeasurement)
        return summarizedMeasurement
    }

    private fun createSummarizedMeasurement(row: ResultRow) = SummarizedMeasurement(
        stationId = StationId.of(row[SummarizedMeasurementsTable.stationId]),
        interval = DateInterval(
            row[SummarizedMeasurementsTable.firstDay],
            row[SummarizedMeasurementsTable.lastDay],
            DateIntervalType.valueOf(row[SummarizedMeasurementsTable.intervalType])
        )
    )

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
        fun day(time: DateTime): DateInterval {
            return DateInterval(
                time.withTimeAtStartOfDay(),
                time.withTimeAtStartOfDay(),
                DateIntervalType.DAY
            )
        }

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

        fun month(time: DateTime) = month(time.year, time.monthOfYear)

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

        fun year(time: DateTime): DateInterval {
            val fromDate = LocalDate(time.year, 1, 1)
            val toDate = fromDate.plusYears(1).minusDays(1)
            return DateInterval(
                fromDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                toDate.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                DateIntervalType.YEAR
            )
        }

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
