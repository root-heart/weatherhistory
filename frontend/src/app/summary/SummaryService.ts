import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {formatDate, registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

export type SummaryJson = {
    firstDay: string,
    lastDay: string,
    intervalType: string,
    countCloudCoverage0: number,
    countCloudCoverage1: number,
    countCloudCoverage2: number,
    countCloudCoverage3: number,
    countCloudCoverage4: number,
    countCloudCoverage5: number,
    countCloudCoverage6: number,
    countCloudCoverage7: number,
    countCloudCoverage8: number,
    countCloudCoverageNotVisible: number,
    countCloudCoverageNotMeasured: number,
    minDewPointTemperatureCentigrade: number,
    maxDewPointTemperatureCentigrade: number,
    avgDewPointTemperatureCentigrade: number,
    minAirTemperatureCentigrade: number,
    maxAirTemperatureCentigrade: number,
    avgAirTemperatureCentigrade: number,
    sumRainfallMillimeters: number,
    sumSnowfallCentimeters: number,
    sumSunshineDurationHours: number,
};

export type SummaryList = Array<SummaryJson>

@Injectable({providedIn: "root"})
export class SummaryService {
    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getSummary(stationId: bigint, fromYear: number, toYear: number): Observable<SummaryList> {
        return this.http.get<SummaryList>('http://localhost:8080/summary/' + stationId +
            '?from=' + fromYear + '-01-01&to=' + toYear + '-12-31');
    }
}