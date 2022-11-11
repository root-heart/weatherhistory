import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

export type CloudCoverageJson = {
    measurementTime: Date,
    cloudCoverage: number
}

export type SummaryJson = {
    firstDay: Date,
    lastDay: Date,
    intervalType: string,
    coverages: Array<number>,
    minDewPointTemperatureCentigrade: number,
    maxDewPointTemperatureCentigrade: number,
    avgDewPointTemperatureCentigrade: number,
    minAirTemperatureCentigrade: number,
    maxAirTemperatureCentigrade: number,
    avgAirTemperatureCentigrade: number,
    sumRainfallMillimeters: number,
    sumSnowfallMillimeters: number,
    sumSunshineDurationHours: number,
    minAirPressureHectopascals: number,
    avgAirPressureHectopascals: number,
    maxAirPressureHectopascals: number,
    avgWindSpeedMetersPerSecond: number,
    maxWindSpeedMetersPerSecond: number,
};

export type SummaryList = Array<SummaryJson>


export type YearlyData = {
    year: number,
    station: string,

    minAirTemperature: number | null,
    minAirTemperatureDay: Date | null,

    avgAirTemperature: number | null,

    maxAirTemperature: number | null,
    maxAirTemperatureDay: Date | null,

    minAirPressureHectopascals: number | null,
    minAirPressureDay: Date | null,

    avgAirPressureHectopascals: number | null,

    maxAirPressureHectopascals: number | null,
    maxAirPressureDay: Date | null,

    avgWindSpeedMetersPerSecond: number | null,
    maxWindSpeedMetersPerSecond: number | null,
    maxWindSpeedDay: Date | null,

    sumRain: number | null,
    sumSnow: number | null,
    sumSunshine: number | null,

    dailyData: Array<DailyData>
}

export type DailyData = {
    day: Date,

    minAirTemperatureCentigrade: number | null,
    avgAirTemperatureCentigrade: number | null,
    maxAirTemperatureCentigrade: number | null,

    minDewPointTemperatureCentigrade: number | null,
    maxDewPointTemperatureCentigrade: number | null,
    avgDewPointTemperatureCentigrade: number | null,

    minAirPressureHectopascals: number | null,
    avgAirPressureHectopascals: number | null,
    maxAirPressureHectopascals: number | null,

    avgWindSpeedMetersPerSecond: number | null,
    maxWindSpeedMetersPerSecond: number | null,

    cloudCoverages: Array<number>,
    sumSunshineDurationHours: number | null,
    sumRainfallMillimeters: number | null,
    sumSnowfallMillimeters: number | null,
}


@Injectable({providedIn: "root"})
export class SummaryService {
    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getSummary(stationId: bigint, year: number): Observable<YearlyData> {
        return this.http.get<YearlyData>('http://localhost:8080/summary/' + stationId + '?year=' + year);
    }
}
