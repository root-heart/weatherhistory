package rootheart.codes.weatherhistory.database.summarized

//class SummarizedMeasurementEntity(
//        val year: Int,
//        val month: Int?,
//
//        val stationId: Long,
//        val airTemperatureCentigrade: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val dewPointTemperatureCentigrade: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val humidityPercent: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val airPressureHectopascals: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val windSpeedMetersPerSecond: SummarizedAvgMax = SummarizedAvgMax(),
//        val visibilityMeters: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val sunshineMinutes: SummarizedSum = SummarizedSum(),
//        val rainfallMillimeters: SummarizedSum = SummarizedSum(),
//        val snowfallMillimeters: SummarizedSum = SummarizedSum(),
//
//        var detailedCloudCoverage: Array<Array<Int>>? = null,
//        var cloudCoverageHistogram: Array<Int>? = null,
//        var detailedWindDirectionDegrees: Array<Int?>? = null)
//
//class SummarizedMeasurement(
//        val stationId: Long,
//        val airTemperatureCentigrade: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val dewPointTemperatureCentigrade: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val humidityPercent: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val airPressureHectopascals: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val windSpeedMetersPerSecond: SummarizedAvgMax = SummarizedAvgMax(),
//        val visibilityMeters: SummarizedMinAvgMax = SummarizedMinAvgMax(),
//        val sunshineMinutes: SummarizedSum = SummarizedSum(),
//        val rainfallMillimeters: SummarizedSum = SummarizedSum(),
//        val snowfallMillimeters: SummarizedSum = SummarizedSum(),
//
//        var detailedCloudCoverage: Array<Array<Int>>? = null,
//        var cloudCoverageHistogram: Array<Int>? = null,
//        var detailedWindDirectionDegrees: Array<Int?>? = null,
//)
//
//class MonthlySummary(
//        val year: Int,
//        val month: Int,
//        val measurements: SummarizedMeasurement
//)
//
//class YearlySummary(
//        val year: Int,
//        val measurements: SummarizedMeasurement
//)