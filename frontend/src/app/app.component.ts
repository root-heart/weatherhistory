import {Component, ViewChild} from '@angular/core';
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {Duration} from "luxon";

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

    columnCount: number = 3

    showDetails(measurementType: MeasurementTypes) {
        this.measurementType = measurementType
    }

    divideBy60(x?: number): number | undefined {
        return x ? x / 60 : undefined
    }

    sumOfCloudCoverage(coverageHistogram: number[] | undefined, coverageIndices: number[]): number {
        if (!coverageHistogram) {
            return 0
        }
        let sum = 0
        for (let i = 0; i < coverageIndices.length; i++) {
            sum += coverageHistogram[coverageIndices[i]]
        }
        return sum
    }
}

