import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

export type WeatherStation = {
    id: number,
    name: string,
    federalState: string
}

export type WeatherStationList = Array<WeatherStation>;

@Injectable({providedIn: "root"})
export class WeatherStationService {
    constructor(private http: HttpClient) {
    }

    getWeatherStations(): Observable<WeatherStationList> {
        return this.http.get<WeatherStationList>('http://localhost:8080/stations/');
    }

    getStationInfo(stationId: bigint): Observable<WeatherStation> {
        return this.http.get<WeatherStation>('http://localhost:8080/stations/' + stationId);
    }
}