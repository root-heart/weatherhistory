import {HttpClient} from "@angular/common/http";
import {registerLocaleData} from "@angular/common";
import localeDe from "@angular/common/locales/de";
import localeDeExtra from "@angular/common/locales/extra/de";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";

export class DataService<R> {
    constructor(private http: HttpClient, private path: string) {
        registerLocaleData(localeDe, 'de-DE', localeDeExtra);
    }

    getDailyData(stationId: bigint, year: number, month: number | null = null): Observable<R[]> {
        return month == null
            ? this.http.get<R[]>(environment.apiServer + "/stations/" + stationId + "/" + this.path + "/daily/" + year)
            : this.http.get<R[]>(environment.apiServer + "/stations/" + stationId + "/" + this.path + "/daily/" + year + "/" + month)
    }

    getMonthlyData(stationId: bigint, year: number): Observable<R[]> {
        return this.http.get<R[]>(environment.apiServer + "/stations/" + stationId + "/" + this.path + "/monthly/" + year)
    }
}
