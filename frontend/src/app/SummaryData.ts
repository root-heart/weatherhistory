import {BehaviorSubject, Subject} from "rxjs";
import {WeatherStation} from "./WeatherStationService";
import {DateTime} from "luxon";
import {ChartResolution} from "./charts/BaseChart";

export type Measurement = {
    maxTemperature?: number,
    rainfall?: number,
    maxVisibility?: number,
    snowfall?: number,
    minDewPointTemperature?: number,
    avgAirPressure?: number,
    maxAirPressure?: number,
    cloudCoverage?: number[],
    minAirPressure?: number,
    avgDewPointTemperature?: number,
    avgHumidity?: number,
    maxWindspeed?: number,
    maxDewPointTemperature?: number,
    minHumidity?: number,
    sunshineDuration?: number,
    avgWindspeed?: number,
    // It does not matter if I use a Date here. TypeScript will stupidly create a Measurement with a string for the
    // property firstDay. So when processing this Measurement in other places in the code, I will see a member of
    // type Date but with a string in it.
    // In every other language I know it is not possible to do so, how sick is this...??
    firstDay: string,
    minTemperature?: number,
    minVisibility?: number,
    avgVisibility?: number,
    maxHumidity?: number,
    avgTemperature?: number
}

export type SummaryData = {
    summary: Measurement,
    details: Measurement[],
    resolution: ChartResolution
}

