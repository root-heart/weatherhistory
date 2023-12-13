import {Injectable} from '@angular/core';
import {WeatherStation} from "../WeatherStationService";
import {HttpClient} from "@angular/common/http";
import {environment} from "../environments/environment";
import {SummaryData} from "../data-classes";
import {firstValueFrom} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class FetchMeasurementsService {
    constructor(private http: HttpClient) {
    }

    fetchMeasurements(weatherStation: WeatherStation, year: number): Promise<SummaryData> {
        let url = `${environment.apiServer}/stations/${weatherStation.id}/summary/${year}`
        console.log(`fetching data from ${url}`)
        return firstValueFrom(this.http.get<SummaryData>(url))
    }
}
