import {Component, ViewChild} from '@angular/core';
import {TemperatureChart} from "./summary/temperature-chart/temperature-chart.component";
import {SunshineChart} from "./summary/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {SummaryList, SummaryService} from "./summary/SummaryService";
import {CloudinessChart} from "./summary/cloudiness-chart/cloudiness-chart.component";
import {Meteogram} from "./meteogram/meteogram.component";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'Wie war das Wetter';

    // @ViewChild('temperatureChart')
    // temperatureChart?: TemperatureChart;

    // @ViewChild('sunshineChart')
    // sunshineChart?: SunshineChart;
    //
    // @ViewChild('cloudinessChart')
    // cloudinessChart?: CloudinessChart;

    @ViewChild('meteogram')
    meteogram?: Meteogram;

    constructor(private summaryService: SummaryService) {

    }

    filterChanged(event: FilterChangedEvent) {
        this.summaryService.getSummary(event.station.id, event.start, event.end)
            .subscribe(data => this.updateAllCharts(data));
    }


    private updateAllCharts(data: SummaryList) {
        // Yes, the member is defined as Date. Yes, the data send by the server comes in a format that typescript can
        // recognize as a Date. No, typescript does not automatically create a Date but rather puts a String into the
        // member that is a Date. So I have to do it on my own. Jeez...
        data.forEach(summaryJson => {
            summaryJson.firstDay = new Date(summaryJson.firstDay)
            summaryJson.lastDay = new Date(summaryJson.lastDay)
            summaryJson.lastDay.setHours(23)
            summaryJson.lastDay.setMinutes(59)
            summaryJson.lastDay.setSeconds(59)
            summaryJson.lastDay.setMilliseconds(999)
        })
        // this.temperatureChart?.setData(data);
        // this.sunshineChart?.setData(data);
        // this.cloudinessChart?.setData(data);
        this.meteogram?.setData(data)
    }
}

