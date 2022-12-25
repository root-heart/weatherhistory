import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {registerLocaleData} from "@angular/common";
import localeDe from "@angular/common/locales/de";
import localeDeExtra from "@angular/common/locales/extra/de";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {CloudinessRecord} from "./cloudiness-chart.component";

@Injectable({
  providedIn: 'root'
})
export class CloudinessDataService {
    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getHourlyData(stationId: bigint, year: number, month: number | null = null): Observable<CloudinessRecord[]> {
        return month == null
            ? this.http.get<CloudinessRecord[]>(environment.apiServer + "/stations/" + stationId + "/cloudCoverage/hourly/" + year)
            : this.http.get<CloudinessRecord[]>(environment.apiServer + "/stations/" + stationId + "/cloudCoverage/hourly/" + year + "/" + month)
    }
}
