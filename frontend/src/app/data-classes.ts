import {BehaviorSubject, Subject} from "rxjs";
import {WeatherStation} from "./WeatherStationService";
import {DateTime} from "luxon";
import {ChartResolution} from "./charts/BaseChart";

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

export class DailyMeasurement implements WithDate {
    // It does not matter if I use a Date here. TypeScript will stupidly create a Measurement with a string for the
    // property firstDay. So when processing this Measurement in other places in the code, I will see a member of
    // type Date but with a string in it.
    // In every other language I know it is not possible to do so, how sick is this...??
    date?: string

    measurements?: SummarizedMeasurement

    getDate(): string {
        return this.date!
    }

}

export interface WithDate {
    getDate(): string

}

export class YearlySummary implements WithDate {
    year?: number
    measurements?: SummarizedMeasurement

    getDate(): string {
        return this.year!.toPrecision(0)
    }
}

export class MonthlySummary implements WithDate {
    year?: number
    month?: number
    measurements?: SummarizedMeasurement

    getDate(): string {
        return this.year!.toPrecision(0) + "-" + this.month!.toPrecision(0)
    }
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
    detailedWindDirectionDegrees: number[],
}


export type SummaryData = {
    summary: SummarizedMeasurement,
    details: DailyMeasurement[] | MonthlySummary[] | YearlySummary[],
    resolution: ChartResolution
}

