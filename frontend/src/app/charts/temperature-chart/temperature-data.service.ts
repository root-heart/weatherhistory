import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {registerLocaleData} from "@angular/common";
import localeDe from "@angular/common/locales/de";
import localeDeExtra from "@angular/common/locales/extra/de";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {BaseRecord} from "../BaseChart";

export type TemperatureRecord = BaseRecord & {
    avgAirTemperatureCentigrade: number,
    maxAirTemperatureCentigrade: number,
    minAirTemperatureCentigrade: number,
}

export type MonthlyTempRecord = BaseRecord & {
    avgAirTemperatureCentigrade: number,
    maxAirTemperatureCentigrade: number,
    minAirTemperatureCentigrade: number,
}

@Injectable({
    providedIn: 'root'
})
export class TemperatureDataService {

    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getDailyData(stationId: bigint, year: number, month: number | null = null): Observable<TemperatureRecord[]> {
        return month == null
            ? this.http.get<TemperatureRecord[]>(environment.apiServer + "/stations/" + stationId + "/temperature/daily/" + year)
            : this.http.get<TemperatureRecord[]>(environment.apiServer + "/stations/" + stationId + "/temperature/daily/" + year + "/" + month)
    }

    getMonthlyData(stationId: bigint, year: number): Observable<MonthlyTempRecord[]> {
        return this.http.get<MonthlyTempRecord[]>(environment.apiServer + "/stations/" + stationId + "/temperature/monthly/" + year)
    }
}
