import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {
    faCloud,
    faCloudShowersHeavy,
    faCloudSun,
    faSnowflake,
    faSquare,
    faSquareXmark,
    faSun
} from '@fortawesome/free-solid-svg-icons';
import {currentData, DateRangeFilter} from "./SummaryData";
import {ChartResolution} from "./charts/BaseChart";
import {FilterService} from "./filter.service";
import {DropdownService} from "./dropdown.service";

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

    measurementType?: MeasurementTypes

    faSun = faSun
    faCloudSun = faCloudSun
    faCloud = faCloud
    faRain = faCloudShowersHeavy
    faSnow = faSnowflake
    faSquare = faSquare
    faSquareChecked = faSquareXmark

    currentData = currentData


    DateRangeFilter = DateRangeFilter

    @ViewChild("dropdownBackground") dropdownBackground?: ElementRef

    constructor(public filterService: FilterService, private dropdownService: DropdownService) {
    }

    ngAfterViewInit() {
        this.dropdownService.dropdownBackground = this.dropdownBackground
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
        if (this.filterService.dateRangeFilter == DateRangeFilter.MONTHLY) {
            return "daily"
        } else {
            return "monthly"
        }
    }


    filter(range: DateRangeFilter) {
        this.filterService.dateRangeFilter = range
        this.filterService.fireFilterChangedEvent()
    }

    getYears(): number[] {
        let start = 1970
        let end = 2023
        return Array.from({length: (end - start)}, (v, k) => k + start)
    }
}

