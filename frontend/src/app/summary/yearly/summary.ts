import {Component, ViewChild} from '@angular/core';
import {SummaryChart} from "./summary-chart";
import {HttpClient} from "@angular/common/http";
import {WeatherStationList, WeatherStationService} from "../../WeatherStationService";
import {SummaryList, SummaryService} from "../SummaryService";
import {TemperatureChart} from "../temperature-chart/temperature-chart.component";
import {SunshineChart} from "../sunshine-chart/sunshine-chart.component";

@Component({
    selector: 'yearly-summary',
    templateUrl: './summary.html',
    styleUrls: ['./summary.css']
})
export class Summary {

    selectedStationId?: bigint;
    weatherStationList: WeatherStationList | null = null;

    @ViewChild('temperatureChart')
    temperatureChart?: TemperatureChart;

    @ViewChild('sunshineChart')
    sunshineChart?: SunshineChart;

    from: Date | null = null;
    to: Date | null = null;

    constructor(private http: HttpClient,
                private weatherStationService: WeatherStationService,
                private summaryService: SummaryService) {
        weatherStationService.getWeatherStations().subscribe(data => this.weatherStationList = data);
    }

    updateChart() {
        if (this.selectedStationId && this.from && this.to) {
            // this.summaryService.getSummary(this.selectedStationId, this.from, this.to)
            //     .subscribe(data => this.updateAllCharts(data));
        }
    }

    private updateAllCharts(data: SummaryList) {
        this.temperatureChart?.setData(data);
        this.sunshineChart?.setData(data);
    }
}