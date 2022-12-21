import {Component, ViewChild} from '@angular/core';
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";

export type MeasurementTypes = "temperature" | "humidity" | "airPressure" | "visibility"

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
})
export class AppComponent {
    title = 'wetterchroniken.de/';

    @ViewChild('cloudinessChart')
    cloudinessChart?: CloudinessChart

    measurementType?: MeasurementTypes

    columnCount: number = 1

    showDetails(measurementType: MeasurementTypes) {
        this.measurementType = measurementType
    }
}

