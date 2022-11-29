import {Component, ViewChild} from '@angular/core';
import {TemperatureChart} from "./charts/temperature-chart/temperature-chart.component";
import {SunshineChart} from "./charts/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {PrecipitationChart} from "./charts/precipitation-chart/precipitation-chart.component";
import {AirPressureChart} from "./charts/air-pressure-chart/air-pressure-chart.component";
import {WindSpeedChart} from "./charts/wind-speed-chart/wind-speed-chart.component";
import {TemperatureDataService} from "./charts/temperature-chart/temperature-data.service";
import {CloudinessDataService} from "./charts/cloudiness-chart/cloudiness-data.service";
import {AirPressureDataService} from "./charts/air-pressure-chart/air-pressure-data.service";
import {PrecipitationDataService} from "./charts/precipitation-chart/precipitation-data.service";
import {SunshineDurationDataService} from "./charts/sunshine-chart/sunshine-duration-data.service";
import {WindSpeedDataService} from "./charts/wind-speed-chart/wind-speed-data.service";

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


    constructor(private temperatureDataService: TemperatureDataService,
                private cloudinessDataService: CloudinessDataService,
                private airPressureDataService: AirPressureDataService,
                private precipitationDataService: PrecipitationDataService,
                private sunshineDurationDataService : SunshineDurationDataService,
                private windSpeedDataService: WindSpeedDataService) {

    }

    filterChanged(event: FilterChangedEvent) {
        let stationId = event.station.id;
        let year = event.start;
        this.temperatureDataService.getDailyData(stationId, year)
            .subscribe(data => this.temperatureChart?.setData(data));
        this.cloudinessDataService.getHourlyData(stationId, year)
            .subscribe(data => this.cloudinessChart?.setData(data))
        this.airPressureDataService.getDailyData(stationId, year)
            .subscribe(data => this.airPressureChart?.setData(data))
        this.precipitationDataService.getDailyData(stationId, year)
            .subscribe(data => this.precipitationChart?.setData(data))
        this.sunshineDurationDataService.getDailyData(stationId, year)
            .subscribe(data => this.sunshineChart?.setData(data))
        this.windSpeedDataService.getDailyData(stationId, year)
            .subscribe(data => this.windSpeedChart?.setData(data))
    }


    // private updateAllCharts(yearlyData: YearlyData) {
    //     // Yes, the member is defined as Date. Yes, the data send by the server comes in a format that typescript can
    //     // recognize as a Date. No, typescript does not automatically create a Date but rather puts a String into the
    //     // member that is a Date. So I have to do it on my own. Jeez...
    //     yearlyData.dailyData.forEach(dailyData => {
    //         dailyData.day = new Date(dailyData.day)
    //         // summaryJson.lastDay = new Date(summaryJson.lastDay)
    //         // summaryJson.lastDay.setHours(23)
    //         // summaryJson.lastDay.setMinutes(59)
    //         // summaryJson.lastDay.setSeconds(59)
    //         // summaryJson.lastDay.setMilliseconds(999)
    //     })
    //     // this.meteogram?.setData(summaryList)
    //     this.temperatureChart?.setData(yearlyData.dailyData)
    //     this.sunshineChart?.setData(yearlyData.dailyData)
    //     this.precipitationChart?.setData(yearlyData.dailyData)
    //     this.airPresssurceChart?.setData(yearlyData.dailyData)
    //     this.cloudinessChart?.setData(yearlyData.dailyData)
    //     this.windSpeedChart?.setData(yearlyData.dailyData)
    //
    //     this.minTemperature = yearlyData.minAirTemperature
    //     this.maxTemperature = yearlyData.maxAirTemperature
    //     this.avgTemperature = yearlyData.avgAirTemperature
    //
    //     this.sumSunshineDuration = yearlyData.sumSunshine?.toFixed(1)
    // }
}

