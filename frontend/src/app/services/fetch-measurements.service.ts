import {Injectable} from '@angular/core';
import {WeatherStation} from "../WeatherStationService";
import {HttpClient} from "@angular/common/http";
import {environment} from "../environments/environment";
import {SummaryData} from "../data-classes";
import {firstValueFrom} from "rxjs";

export abstract class FMS {
    protected constructor(protected http: HttpClient) {
    }

    protected abstract getMeasurementName(): string

    fetchData(weatherStation: WeatherStation, year: number): Promise<SummaryData> {
        let url = `${environment.apiServer}/stations/${weatherStation.id}/${this.getMeasurementName()}/${year}`
        console.log(`fetching data from ${url}`)
        return firstValueFrom(this.http.get<SummaryData>(url))
    }
}

@Injectable({
    providedIn: 'root'
})
export class FetchAirTemperatureService extends FMS{
    constructor(http: HttpClient) {
        super(http)
    }

    protected override getMeasurementName(): string {
        return "air-temperature";
    }
}
