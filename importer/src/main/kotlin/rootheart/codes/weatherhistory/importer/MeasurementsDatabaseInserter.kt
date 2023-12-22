package rootheart.codes.weatherhistory.importer

import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import rootheart.codes.common.collections.AvgMaxDetails
import rootheart.codes.common.collections.MinAvgMaxDetails
import rootheart.codes.common.collections.MinMaxSumDetails
import rootheart.codes.weatherhistory.database.AvgMaxDetailsColumns
import rootheart.codes.weatherhistory.database.MinAvgMaxDetailsColumns
import rootheart.codes.weatherhistory.database.MinMaxSumDetailsColumns
import rootheart.codes.weatherhistory.database.StationsTable
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementEntity
import rootheart.codes.weatherhistory.database.daily.DailyMeasurementTable

private val log = KotlinLogging.logger {}

private val existing: MutableSet<String> = HashSet()

fun insertDailyMeasurementsIntoDatabase(measurements: List<DailyMeasurementEntity>) = transaction {
    val stationIds = measurements.mapNotNull { it.stationId }.distinct()
    val stationIdById = StationsTable.select { StationsTable.id.inList(stationIds) }
            .map { row -> row[StationsTable.id] }
            .associateBy { it.value }
    with (DailyMeasurementTable) {
        batchInsert(measurements) {
            val date = org.joda.time.LocalDateTime(it.dateInUtcMillis)
            this[stationId] = stationIdById[it.stationId]!!
            this[year] = date.year
            this[month] = date.monthOfYear
            this[day] = date.dayOfMonth

            val tuple = "${it.stationId}, ${date.year}, ${date.monthOfYear}, ${date.dayOfMonth}"
            if (existing.contains(tuple)) {
                println("Found duplicate: $existing")
            } else {
                existing.add(tuple)
            }

            airTemperatureCentigrade.setValues(this, it.airTemperatureCentigrade)
            dewPointTemperatureCentigrade.setValues(this, it.dewPointTemperatureCentigrade)
            humidityPercent.setValues(this, it.humidityPercent)
            airPressureHectopascals.setValues(this, it.airPressureHectopascals)
            visibilityMeters.setValues(this, it.visibilityMeters)
            windSpeedMetersPerSecond.setValues(this, it.windSpeedMetersPerSecond)

            this[cloudCoverageHistogram] = it.cloudCoverage.histogram ?: Array(0) { 0 }
            this[detailedCloudCoverage] = it.cloudCoverage.details

            sunshineMinutes.setValues(this, it.sunshineMinutes)
            rainfallMillimeters.setValues(this, it.rainfallMillimeters )
            snowfallMillimeters.setValues(this, it.snowfallMillimeters)

            windDirectionDegrees.setValues(this, it.windDirectionDegrees)
        }
    }
    log.info { "Inserted ${measurements.size} objects into the database" }
}

//fun insertSummarizedMeasurementsIntoDatabase(measurements: List<SummarizedMeasurementEntity>) = transaction {
//    val stationIds = measurements.mapNotNull { it.stationId }.distinct()
//    val stationIdById = StationsTable.select { StationsTable.id.inList(stationIds) }
//            .map { row -> row[StationsTable.id] }
//            .associateBy { it.value }
//    with (SummarizedMeasurementsTable) {
//        batchInsert(measurements) {
//            this[stationId] = stationIdById[it.stationId]!!
//            this[year] = it.year
//            this[month] = it.month
//
//            airTemperatureCentigrade.setValues(this, it.airTemperatureCentigrade)
//            dewPointTemperatureCentigrade.setValues(this, it.dewPointTemperatureCentigrade)
//            humidityPercent.setValues(this, it.humidityPercent)
//            airPressureHectopascals.setValues(this, it.airPressureHectopascals)
//            visibilityMeters.setValues(this, it.visibilityMeters)
//            windSpeedMetersPerSecond.setValues(this, it.windSpeedMetersPerSecond)
//
//            this[cloudCoverageHistogram] = it.cloudCoverageHistogram ?: Array(0) { 0 }
//            this[detailedCloudCoverage] = it.detailedCloudCoverage
//
//            sunshineMinutes.setValues(this, it.sunshineMinutes)
//            rainfallMillimeters.setValues(this, it.rainfallMillimeters)
//            snowfallMillimeters.setValues(this, it.snowfallMillimeters)
//
//            this[detailedWindDirectionDegrees] = it.detailedWindDirectionDegrees
//        }
//    }
//    log.info { "Inserted ${measurements.size} objects into the database" }
//}

private fun <N : Number> BatchInsertStatement.copyMinAvgMax(from: MinAvgMaxDetails<N>, to: MinAvgMaxDetailsColumns<N>) {
    this[to.min] = from.min
    this[to.minDay] = from.minDay?.toDateTimeAtStartOfDay()
    this[to.avg] = from.avg
    this[to.max] = from.max
    this[to.maxDay] = from.maxDay?.toDateTimeAtStartOfDay()
    this[to.details] = from.details
}

private fun <N : Number> BatchInsertStatement.copyAvgMax(from: AvgMaxDetails<N>, to: AvgMaxDetailsColumns<N>) {
    this[to.avg] = from.avg
    this[to.max] = from.max
    this[to.maxDay] = from.maxDay?.toDateTimeAtStartOfDay()
    this[to.details] = from.details
}

private fun <N : Number> BatchInsertStatement.copySum(from: MinMaxSumDetails<N>, to: MinMaxSumDetailsColumns<N>) {
    this[to.min] = from.min
    this[to.minDay] = from.minDay?.toDateTimeAtStartOfDay()
    this[to.max] = from.max
    this[to.maxDay] = from.maxDay?.toDateTimeAtStartOfDay()
    this[to.sum] = from.sum
    this[to.details] = from.details
}

