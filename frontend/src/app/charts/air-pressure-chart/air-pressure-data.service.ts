import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {registerLocaleData} from "@angular/common";
import localeDe from "@angular/common/locales/de";
import localeDeExtra from "@angular/common/locales/extra/de";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {AirPressureRecord} from "./air-pressure-chart.component";

@Injectable({
  providedIn: 'root'
})
export class AirPressureDataService {

    constructor(private http: HttpClient) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getDailyData(stationId: bigint, year: number, month: number | null = null): Observable<AirPressureRecord[]> {
        return month == null
            ? this.http.get<AirPressureRecord[]>(environment.apiServer + "/stations/" + stationId + "/airPressure/daily/" + year)
            : this.http.get<AirPressureRecord[]>(environment.apiServer + "/stations/" + stationId + "/airPressure/daily/" + year + "/" + month)
    }
}
