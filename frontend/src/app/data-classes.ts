export type ChartResolution = "day" | "month" | "year"

export type MinMaxDetails = {
    min: number,
    max: number, maxDay?: Date,
    details?: number[]
}

export type AvgMaxDetails = {
    avg: number,
    max: number, maxDay?: Date,
    details?: number[]
}

export type MinAvgMaxDetails = {
    min: number, minDay?: Date,
    avg: number,
    max: number, maxDay?: Date,
    details: number[]
}

export type MinMaxSumDetails = {
    min?: number, minDay: Date,
    max?: number, maxDay: Date,
    sum?: number,
    details?: number[]
}

export type DailyMeasurement = { dateInUtcMillis: number } & SummarizedMeasurement

export type YearlySummary = { year?: number } & SummarizedMeasurement

export type MonthlySummary = { year?: number, month?: number } & SummarizedMeasurement

export type SummarizedMeasurement = {
    airTemperatureCentigrade: MinAvgMaxDetails,
    dewPointTemperatureCentigrade: MinAvgMaxDetails,
    humidityPercent: MinAvgMaxDetails,
    airPressureHectopascals: MinAvgMaxDetails,
    windSpeedMetersPerSecond: AvgMaxDetails,
    visibilityMeters: MinAvgMaxDetails,
    sunshineMinutes: MinMaxSumDetails,
    rainfallMillimeters: MinMaxSumDetails,
    snowfallMillimeters: MinMaxSumDetails,
    detailedCloudCoverage: number[],
    cloudCoverageHistogram: number[],
    windDirectionDegrees: MinMaxDetails,
}


export type SummaryData = {
    summary: SummarizedMeasurement,
    details: DailyMeasurement[] | MonthlySummary[] | YearlySummary[],
    resolution: ChartResolution
}

