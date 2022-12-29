import {Component, ViewChild} from '@angular/core';
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {
    faCalendar,
    faCloud,
    faCloudShowersHeavy,
    faCloudSun,
    faMapLocationDot,
    faSnowflake, faSquareXmark,
    faSun, faSquare
} from '@fortawesome/free-solid-svg-icons';
import {currentData, currentFilter, DateRangeFilter, SummaryData} from "./SummaryData";
import {ChartResolution} from "./charts/BaseChart";
import * as luxon from "luxon";
import {environment} from "../environments/environment";
import {FilterService} from "./filter.service";

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

    faSun = faSun
    faCloudSun = faCloudSun
    faCloud = faCloud
    faRain = faCloudShowersHeavy
    faSnow = faSnowflake
    currentData = currentData
    faSquare = faSquare
    faSquareChecked = faSquareXmark
    DateRangeFilter = DateRangeFilter

    constructor(public filterService: FilterService) {
    }

    showDetails(measurementType: MeasurementTypes) {
        this.measurementType = measurementType
    }

    divideBy60(x?: number): number | undefined {
        return x ? x / 60 : undefined
    }

    percentageOfCloudCoverage(coverageHistogram: number[] | undefined, coverageIndices: number[]): string {
        if (!coverageHistogram) {
            return ""
        }

        let part = 0
        let sum = 0
        for (let i = 0; i < coverageHistogram.length; i++) {
            if (coverageIndices.indexOf(i) !== -1) {
                part += coverageHistogram[i]
            }
            sum += coverageHistogram[i]
        }
        return (part / sum * 100).toFixed(1) + "%"
    }



    getChartResolution(): ChartResolution {
        if (this.filterService.dateRangeFilter == DateRangeFilter.LAST_MONTH || this.filterService.dateRangeFilter == DateRangeFilter.THIS_MONTH) {
            return "daily"
        } else {
            return "monthly"
        }
    }


    filter(range: DateRangeFilter) {
        this.filterService.dateRangeFilter = range
        this.filterService.fireFilterChangedEvent()
    }
}

