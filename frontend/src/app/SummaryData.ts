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

export type Measurement = {
    // It does not matter if I use a Date here. TypeScript will stupidly create a Measurement with a string for the
    // property firstDay. So when processing this Measurement in other places in the code, I will see a member of
    // type Date but with a string in it.
    // In every other language I know it is not possible to do so, how sick is this...??
    firstDay: string,

    temperature: MinAvgMaxDetails,
    dewPointTemperature: MinAvgMaxDetails,
    humidity: MinAvgMaxDetails,
    airPressure: MinAvgMaxDetails,
    cloudCoverage: number[],
    sunshineDuration: MinMaxSumDetails,
    rainfall: MinMaxSumDetails,
    snowfall: MinMaxSumDetails,
    windSpeed: AvgMaxDetails,
    visibility: MinAvgMaxDetails,
}

export type SummarizedMeasurement = {
    temperature: MinAvgMaxDetails,
    dewPointTemperature: MinAvgMaxDetails,
    humidity: MinAvgMaxDetails,
    airPressure: MinAvgMaxDetails,
    cloudCoverage: number[],
    sunshineDuration: MinMaxSumDetails,
    rainfall: MinMaxSumDetails,
    snowfall: MinMaxSumDetails,
    windSpeed: AvgMaxDetails,
    visibility: MinAvgMaxDetails,
}


export type SummaryData = {
    summary: SummarizedMeasurement,
    details: Measurement[],
    resolution: ChartResolution
}

