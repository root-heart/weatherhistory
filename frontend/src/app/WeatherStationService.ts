import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

export type WeatherStation = {
    id: bigint,
    name: string,
    federalState: string,
    latitude: number,
    longitude: number,
    height: number,
    firstMeasurementDateString: string,
    lastMeasurementDateString: string
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
