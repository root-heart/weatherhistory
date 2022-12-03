import {Component, ElementRef, ViewChild} from '@angular/core';
import {SunshineChart, SunshineDurationRecord} from "./charts/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {PrecipitationChart, PrecipitationRecord} from "./charts/precipitation-chart/precipitation-chart.component";
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

    @ViewChild('precipitationChart')
    precipitationChart?: PrecipitationChart

    @ViewChild('sunshineChart')
    sunshineChart?: SunshineChart

    @ViewChild('cloudinessChart')
    cloudinessChart?: CloudinessChart

    @ViewChild('windSpeedChart')
    windSpeedChart?: WindSpeedChart

    measurementType?: MeasurementTypes

    minTemperature: number | null = NaN
    avgTemperature: number | null = NaN
    maxTemperature: number | null = NaN

    sumSunshineDuration?: string

    precipitationDataService: DataService<PrecipitationRecord>;
    sunshineDurationDataService: DataService<SunshineDurationRecord>;
    windSpeedDataService: DataService<WindSpeedRecord>;

    constructor(http: HttpClient) {
        this.precipitationDataService = new DataService<PrecipitationRecord>(http, "precipitation")
        this.sunshineDurationDataService = new DataService<SunshineDurationRecord>(http, "sunshine")
        this.windSpeedDataService = new DataService<WindSpeedRecord>(http, "wind")
    }

    filterChanged(event: FilterChangedEvent) {
        let stationId = event.station.id;
        let year = event.start;
        this.precipitationDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.precipitationChart?.setData(data, "monthly"))
        this.sunshineDurationDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.sunshineChart?.setData(data, "monthly"))
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

