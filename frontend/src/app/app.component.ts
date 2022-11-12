import {Component, ViewChild} from '@angular/core';
import {TemperatureChart} from "./charts/temperature-chart/temperature-chart.component";
import {SunshineChart} from "./charts/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {SummaryList, SummaryService, YearlyData} from "./charts/SummaryService";
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {PrecipitationChart} from "./charts/precipitation-chart/precipitation-chart.component";
import {AirPressureChart} from "./charts/air-pressure-chart/air-pressure-chart.component";
import {WindSpeedChart} from "./charts/wind-speed-chart/wind-speed-chart.component";

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
    airPresssurceChart?: AirPressureChart

    @ViewChild('cloudinessChart')
    cloudinessChart?: CloudinessChart

    @ViewChild('windSpeedChart')
    windSpeedChart?: WindSpeedChart

    minTemperature: number | null= NaN
    avgTemperature: number | null= NaN
    maxTemperature: number | null = NaN

    sumSunshineDuration?: string


    constructor(private summaryService: SummaryService) {

    }

    filterChanged(event: FilterChangedEvent) {
        this.summaryService.getSummary(event.station.id, event.start)
            .subscribe(data => this.updateAllCharts(data));
    }


    private updateAllCharts(yearlyData: YearlyData) {
        // Yes, the member is defined as Date. Yes, the data send by the server comes in a format that typescript can
        // recognize as a Date. No, typescript does not automatically create a Date but rather puts a String into the
        // member that is a Date. So I have to do it on my own. Jeez...
        yearlyData.dailyData.forEach(dailyData => {
            dailyData.day = new Date(dailyData.day)
            // summaryJson.lastDay = new Date(summaryJson.lastDay)
            // summaryJson.lastDay.setHours(23)
            // summaryJson.lastDay.setMinutes(59)
            // summaryJson.lastDay.setSeconds(59)
            // summaryJson.lastDay.setMilliseconds(999)
        })
        // this.meteogram?.setData(summaryList)
        this.temperatureChart?.setData(yearlyData.dailyData)
        this.sunshineChart?.setData(yearlyData.dailyData)
        this.precipitationChart?.setData(yearlyData.dailyData)
        this.airPresssurceChart?.setData(yearlyData.dailyData)
        this.cloudinessChart?.setData(yearlyData.dailyData)
        this.windSpeedChart?.setData(yearlyData.dailyData)

        this.minTemperature = yearlyData.minAirTemperature
        this.maxTemperature = yearlyData.maxAirTemperature
        this.avgTemperature = yearlyData.avgAirTemperature

        this.sumSunshineDuration = yearlyData.sumSunshine?.toFixed(1)
    }
}

