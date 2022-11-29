import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {registerLocaleData} from "@angular/common";
import localeDe from "@angular/common/locales/de";
import localeDeExtra from "@angular/common/locales/extra/de";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {PrecipitationRecord} from "./precipitation-chart.component";

@Injectable({
  providedIn: 'root'
})
export class PrecipitationDataService {
    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }


    // TODO also consider snowfall!
    getDailyData(stationId: bigint, year: number, month: number | null = null): Observable<PrecipitationRecord[]> {
        return month == null
            ? this.http.get<PrecipitationRecord[]>(environment.apiServer + "/stations/" + stationId + "/rainfall/daily/" + year)
            : this.http.get<PrecipitationRecord[]>(environment.apiServer + "/stations/" + stationId + "/rainfall/daily/" + year + "/" + month)
    }
}
