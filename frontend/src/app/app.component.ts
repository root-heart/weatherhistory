import {Component, ViewChild} from '@angular/core';
import {TemperatureChart, TemperatureRecord} from "./charts/temperature-chart/temperature-chart.component";
import {SunshineChart, SunshineDurationRecord} from "./charts/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {PrecipitationChart, PrecipitationRecord} from "./charts/precipitation-chart/precipitation-chart.component";
import {AirPressureChart, AirPressureRecord} from "./charts/air-pressure-chart/air-pressure-chart.component";
import {WindSpeedChart, WindSpeedRecord} from "./charts/wind-speed-chart/wind-speed-chart.component";
import {HttpClient} from "@angular/common/http";
import {DataService} from "./charts/data-service.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'wetterchroniken.de/';

    @ViewChild('temperatureChart')
    temperatureChart?: TemperatureChart

    @ViewChild('precipitationChart')
    precipitationChart?: PrecipitationChart

    @ViewChild('sunshineChart')
    sunshineChart?: SunshineChart

    @ViewChild('airPressureChart')
    airPressureChart?: AirPressureChart

    @ViewChild('cloudinessChart')
    cloudinessChart?: CloudinessChart

    @ViewChild('windSpeedChart')
    windSpeedChart?: WindSpeedChart

    minTemperature: number | null = NaN
    avgTemperature: number | null = NaN
    maxTemperature: number | null = NaN

    sumSunshineDuration?: string


    private temperatureDataService: DataService<TemperatureRecord>;
    private airPressureDataService: DataService<AirPressureRecord>;
    private precipitationDataService: DataService<PrecipitationRecord>;
    private sunshineDurationDataService: DataService<SunshineDurationRecord>;
    private windSpeedDataService: DataService<WindSpeedRecord>;

    constructor(http: HttpClient) {
        this.temperatureDataService = new DataService<TemperatureRecord>(http, "temperature");
        // this.cloudinessDataService = new DataService<>(http, "")
        this.airPressureDataService = new DataService<AirPressureRecord>(http, "airPressure")
        this.precipitationDataService = new DataService<PrecipitationRecord>(http, "precipitation")
        this.sunshineDurationDataService = new DataService<SunshineDurationRecord>(http, "sunshine")
        this.windSpeedDataService = new DataService<WindSpeedRecord>(http, "wind")

    }

    filterChanged(event: FilterChangedEvent) {
        let stationId = event.station.id;
        let year = event.start;
        // this.temperatureDataService.getDailyData(stationId, year)
        //     .subscribe(data => this.temperatureChart?.setData(data, "daily"));
        // // this.cloudinessDataService.getHourlyData(stationId, year)
        // //     .subscribe(data => this.cloudinessChart?.setData(data))
        // this.airPressureDataService.getDailyData(stationId, year)
        //     .subscribe(data => this.airPressureChart?.setData(data, "daily"))
        // this.precipitationDataService.getDailyData(stationId, year)
        //     .subscribe(data => this.precipitationChart?.setData(data, "daily"))
        // this.sunshineDurationDataService.getDailyData(stationId, year)
        //     .subscribe(data => this.sunshineChart?.setData(data, "daily"))
        // this.windSpeedDataService.getDailyData(stationId, year)
        //     .subscribe(data => this.windSpeedChart?.setData(data, "daily"))

        this.temperatureDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.temperatureChart?.setData(data, "monthly"));
        // this.cloudinessDataService.getHourlyData(stationId, year)
        //     .subscribe(data => this.cloudinessChart?.setData(data))
        this.airPressureDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.airPressureChart?.setData(data, "monthly"))
        this.precipitationDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.precipitationChart?.setData(data, "monthly"))
        this.sunshineDurationDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.sunshineChart?.setData(data, "monthly"))
        this.windSpeedDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.windSpeedChart?.setData(data, "monthly"))
    }
}

