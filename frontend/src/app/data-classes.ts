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

export class DailyMeasurement {
    // It does not matter if I use a Date here. TypeScript will stupidly create a Measurement with a string for the
    // property firstDay. So when processing this Measurement in other places in the code, I will see a member of
    // type Date but with a string in it.
    // In every other language I know it is not possible to do so, how sick is this...??
    date?: string

    measurements?: SummarizedMeasurement
}

export class YearlySummary {
    year?: number
    measurements?: SummarizedMeasurement
}

export class MonthlySummary {
    year?: number
    month?: number
    measurements?: SummarizedMeasurement
}

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

