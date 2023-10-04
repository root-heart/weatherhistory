import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {FilterService} from "./filter.service";
import {DropdownService} from "./dropdown.service";
import {Tab} from "./tab-view/tab-view.component";
import {MinAvgMaxChart} from "./charts/MinAvgMaxChart";

export type MeasurementTypes = "temperature" | "humidity" | "airPressure" | "visibility"

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
})
export class AppComponent implements AfterViewInit {
    title = 'wetterchroniken.de/';

    @ViewChild('cloudinessChart')
    cloudinessChart?: CloudinessChart

    @ViewChild("temperatureChart") temperatureChart?: MinAvgMaxChart
    @ViewChild("qqq") qqq?: any



    @ViewChild("dropdownBackground") dropdownBackground?: ElementRef

    constructor(public filterService: FilterService, private dropdownService: DropdownService) {
    }

    ngAfterViewInit() {
        this.dropdownService.dropdownBackground = this.dropdownBackground
        setTimeout(() => {
            // console.log("ngAfterViewInit")
            // console.log(this.qqq)
            // console.log(this.temperatureChart)
            //
            // this.temperatureChart?.updateChart()

        })
    }
}

