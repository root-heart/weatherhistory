import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {registerLocaleData} from "@angular/common";
import localeDe from "@angular/common/locales/de";
import localeDeExtra from "@angular/common/locales/extra/de";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {WindSpeedRecord} from "./wind-speed-chart.component";

@Injectable({
    providedIn: 'root'
})
export class WindSpeedDataService {
    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getDailyData(stationId: bigint, year: number, month: number | null = null): Observable<WindSpeedRecord[]> {
        return month == null
            ? this.http.get<WindSpeedRecord[]>(environment.apiServer + "/stations/" + stationId + "/wind/daily/" + year)
            : this.http.get<WindSpeedRecord[]>(environment.apiServer + "/stations/" + stationId + "/wind/daily/" + year + "/" + month)
    }
}
