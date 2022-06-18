import {Component, ViewChild} from '@angular/core';
import {TemperatureChart} from "./summary/temperature-chart/temperature-chart.component";
import {SunshineChart} from "./summary/sunshine-chart/sunshine-chart.component";
import {FilterChangedEvent} from "./filter-header/station-and-date-filter.component";
import {SummaryList, SummaryService} from "./summary/SummaryService";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'Wie war das Wetter';

    @ViewChild('temperatureChart')
    temperatureChart?: TemperatureChart;

    @ViewChild('sunshineChart')
    sunshineChart?: SunshineChart;

    constructor(private summaryService: SummaryService) {

    }


    filterChanged(event: FilterChangedEvent) {
        this.summaryService.getSummary(event.station.id, event.start, event.end)
            .subscribe(data => this.updateAllCharts(data));
    }


    private updateAllCharts(data: SummaryList) {
        this.temperatureChart?.setData(data);
        this.sunshineChart?.setData(data);
    }
}

