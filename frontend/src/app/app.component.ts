import {Component, ViewChild} from '@angular/core';
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {WindSpeedChart, WindSpeedRecord} from "./charts/wind-speed-chart/wind-speed-chart.component";
import {HttpClient} from "@angular/common/http";
import {DataService} from "./charts/data-service.service";

export type MeasurementTypes = "temperature" | "humidity" | "airPressure" | "visibility"

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
})
export class AppComponent {
    title = 'wetterchroniken.de/';

    @ViewChild('cloudinessChart')
    cloudinessChart?: CloudinessChart

    @ViewChild('windSpeedChart')
    windSpeedChart?: WindSpeedChart

    measurementType?: MeasurementTypes

    minTemperature: number | null = NaN
    avgTemperature: number | null = NaN
    maxTemperature: number | null = NaN

    sumSunshineDuration?: string
    windSpeedDataService: DataService<WindSpeedRecord>;

    constructor(http: HttpClient) {
        this.windSpeedDataService = new DataService<WindSpeedRecord>(http, "wind")
    }

    filterChanged(event: FilterChangedEvent) {
        let stationId = event.station.id;
        let year = event.start;
        this.windSpeedDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.windSpeedChart?.setData(data, "monthly"))

        // TODO reinstantiate cloud coverage chart
        // this.cloudinessDataService.getHourlyData(stationId, year)
        //     .subscribe(data => this.cloudinessChart?.setData(data))
        // TODO wind direction chart
    }

    showDetails(measurementType: MeasurementTypes) {
        this.measurementType = measurementType
    }
}

