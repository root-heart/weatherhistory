import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {registerLocaleData} from "@angular/common";
import localeDe from "@angular/common/locales/de";
import localeDeExtra from "@angular/common/locales/extra/de";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {SunshineDurationRecord} from "./sunshine-chart.component";

@Injectable({
  providedIn: 'root'
})
export class SunshineDurationDataService {
    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getDailyData(stationId: bigint, year: number, month: number | null = null): Observable<SunshineDurationRecord[]> {
        return month == null
            ? this.http.get<SunshineDurationRecord[]>(environment.apiServer + "/stations/" + stationId + "/sunshine/daily/" + year)
            : this.http.get<SunshineDurationRecord[]>(environment.apiServer + "/stations/" + stationId + "/sunshine/daily/" + year + "/" + month)
    }
}
