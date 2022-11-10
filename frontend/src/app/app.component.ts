import {Component, ViewChild} from '@angular/core';
import {TemperatureChart} from "./charts/temperature-chart/temperature-chart.component";
import {SunshineChart} from "./charts/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {SummaryList, SummaryService} from "./charts/SummaryService";
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

    minTemperature?: number
    avgTemperature?: number
    maxTemperature?: number

    sumSunshineDuration?: string


    constructor(private summaryService: SummaryService) {

    }

    filterChanged(event: FilterChangedEvent) {
        this.summaryService.getSummary(event.station.id, event.start, event.end)
            .subscribe(data => this.updateAllCharts(data));
    }


    private updateAllCharts(summaryList: SummaryList) {
        // Yes, the member is defined as Date. Yes, the data send by the server comes in a format that typescript can
        // recognize as a Date. No, typescript does not automatically create a Date but rather puts a String into the
        // member that is a Date. So I have to do it on my own. Jeez...
        summaryList.forEach(summaryJson => {
            summaryJson.firstDay = new Date(summaryJson.firstDay)
            summaryJson.lastDay = new Date(summaryJson.lastDay)
            summaryJson.lastDay.setHours(23)
            summaryJson.lastDay.setMinutes(59)
            summaryJson.lastDay.setSeconds(59)
            summaryJson.lastDay.setMilliseconds(999)
        })
        // this.meteogram?.setData(summaryList)
        this.temperatureChart?.setData(summaryList)
        this.sunshineChart?.setData(summaryList)
        this.precipitationChart?.setData(summaryList)
        this.airPresssurceChart?.setData(summaryList)
        this.cloudinessChart?.setData(summaryList)
        this.windSpeedChart?.setData(summaryList)

        this.minTemperature = Math.min.apply(Math, summaryList.map(s => s.minAirTemperatureCentigrade))
        this.maxTemperature = Math.max.apply(Math, summaryList.map(s => s.maxAirTemperatureCentigrade))


        this.sumSunshineDuration = summaryList.map(s => s.sumSunshineDurationHours)
            .filter(v => v)
            .reduce((sum, sunshineDurationHours) => sum + sunshineDurationHours, 0)
            .toFixed(1)
    }
}

