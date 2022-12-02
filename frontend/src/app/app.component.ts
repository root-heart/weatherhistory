import {Component, ViewChild} from '@angular/core';
import {TemperatureRecord} from "./charts/temperature-chart/temperature-chart.component";
import {SunshineChart, SunshineDurationRecord} from "./charts/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {PrecipitationChart, PrecipitationRecord} from "./charts/precipitation-chart/precipitation-chart.component";
import {AirPressureChart, AirPressureRecord} from "./charts/air-pressure-chart/air-pressure-chart.component";
import {WindSpeedChart, WindSpeedRecord} from "./charts/wind-speed-chart/wind-speed-chart.component";
import {VisibilityChart, VisibilityRecord} from "./charts/visibility-chart/visibility-chart.component";
import {HttpClient} from "@angular/common/http";
import {DataService} from "./charts/data-service.service";
import {
    DewPointTemperatureChart,
    DewPointTemperatureRecord
} from "./charts/dew-point-temperature-chart/dew-point-temperature-chart.component";
import {HumidityChart, HumidityRecord} from "./charts/humidity-chart/humidity-chart.component";
import {MinAvgMaxChart} from "./charts/BaseChart";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
})
export class AppComponent {
    title = 'wetterchroniken.de/';

    minTemps: number[] = []

    @ViewChild('temperatureChart')
    temperatureChart?: MinAvgMaxChart<TemperatureRecord>

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

    @ViewChild('dewPointTemperatureChart')
    dewPointTemperatureChart?: DewPointTemperatureChart

    @ViewChild('humidityChart')
    humidityChart?: HumidityChart

    @ViewChild('visibilityChart')
    visibilityChart?: VisibilityChart

    @ViewChild('minAvgMaxChart')
    minAvgMaxTempChart?: MinAvgMaxChart<TemperatureRecord>

    minTemperature: number | null = NaN
    avgTemperature: number | null = NaN
    maxTemperature: number | null = NaN

    sumSunshineDuration?: string


    private temperatureDataService: DataService<TemperatureRecord>;
    private airPressureDataService: DataService<AirPressureRecord>;
    private precipitationDataService: DataService<PrecipitationRecord>;
    private sunshineDurationDataService: DataService<SunshineDurationRecord>;
    private windSpeedDataService: DataService<WindSpeedRecord>;
    private dewPointTemperatureDataService: DataService<DewPointTemperatureRecord>;
    private humidityDataService: DataService<HumidityRecord>;
    private visibilityDataService: DataService<VisibilityRecord>;

    constructor(http: HttpClient) {
        this.temperatureDataService = new DataService<TemperatureRecord>(http, "temperature");
        // this.cloudinessDataService = new DataService<>(http, "")
        this.airPressureDataService = new DataService<AirPressureRecord>(http, "airPressure")
        this.precipitationDataService = new DataService<PrecipitationRecord>(http, "precipitation")
        this.sunshineDurationDataService = new DataService<SunshineDurationRecord>(http, "sunshine")
        this.windSpeedDataService = new DataService<WindSpeedRecord>(http, "wind")
        this.dewPointTemperatureDataService = new DataService<DewPointTemperatureRecord>(http, "dewPointTemperature")
        this.humidityDataService = new DataService<HumidityRecord>(http, "humidity")
        this.visibilityDataService = new DataService<VisibilityRecord>(http, "visibility")
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

        const monthlyData = this.temperatureDataService.getMonthlyData(stationId, year)
        monthlyData.subscribe(data => this.temperatureChart?.setData(data, "monthly",
            (r) => r.minAirTemperatureCentigrade,
            (r) => r.avgAirTemperatureCentigrade,
            (r) => r.maxAirTemperatureCentigrade))

        this.airPressureDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.airPressureChart?.setData(data, "monthly"))

        this.precipitationDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.precipitationChart?.setData(data, "monthly"))
        this.sunshineDurationDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.sunshineChart?.setData(data, "monthly"))
        this.windSpeedDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.windSpeedChart?.setData(data, "monthly"))
        this.dewPointTemperatureDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.dewPointTemperatureChart?.setData(data, "monthly"))
        this.humidityDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.humidityChart?.setData(data, "monthly"))
        this.visibilityDataService.getMonthlyData(stationId, year)
            .subscribe(data => this.visibilityChart?.setData(data, "monthly"))
        // TODO visibility chart

        // TODO reinstantiate cloud coverage chart
        // this.cloudinessDataService.getHourlyData(stationId, year)
        //     .subscribe(data => this.cloudinessChart?.setData(data))
        // TODO wind direction chart
    }
}

