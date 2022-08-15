import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {formatDate, registerLocaleData} from "@angular/common";
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
};

export type SummaryList = Array<SummaryJson>

@Injectable({providedIn: "root"})
export class SummaryService {
    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getSummary(stationId: bigint, fromYear: number, toYear: number): Observable<SummaryList> {
        let summaryListObservable = this.http.get<SummaryList>('http://localhost:8080/summary/' + stationId +
            '?year=' + fromYear);
        return summaryListObservable;
    }
}
