import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {FilterService} from "../filter.service";
import {MeasurementTypes} from "../app.component";
import {
    faCalendarWeek,
    faCloud,
    faCloudShowersHeavy,
    faCloudSun,
    faSnowflake,
    faSquare,
    faSquareXmark,
    faSun
} from '@fortawesome/free-solid-svg-icons';
import * as Highcharts from 'highcharts';

import addMore from "highcharts/highcharts-more";
import {getDateLabel} from "../charts/charts";
import {formatDate, registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);

@Component({
    selector: 'station-charts',
    templateUrl: './station-charts.component.html',
    styleUrls: ['./station-charts.component.scss'],
    // encapsulation: ViewEncapsulation.None
})
export class StationChartsComponent {
    faSun = faSun
    faCloudSun = faCloudSun
    faCloud = faCloud
    faRain = faCloudShowersHeavy
    faSnow = faSnowflake
    faSquare = faSquare
    faSquareChecked = faSquareXmark
    faCalendar = faCalendarWeek

    constructor(public filterService: FilterService) {
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

    private uniqueFilter(value: any, index: number, self: number[]) {
        return self.indexOf(value) === index;
    }

    /**
     * It seems that JavaScript seems to sort an array of numbers lexicographically by default, meaning that the number
     * 10 precedes the number 5 (which is absolute f***ing nonsense). To overcome this, I wrote this number comparator.
     */
    private numberComparator(a: number, b: number) {
        return a - b;
    }

    private clearChart(chart: Highcharts.Chart) {
        while (chart.series.length > 0) {
            chart.series[0].remove()
        }
    }

    private indexOfMax(arr: number[]) {
        if (arr.length === 0) {
            return -1;
        }

        let max = arr[0];
        let maxIndex = 0;

        for (let i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                maxIndex = i;
                max = arr[i];
            }
        }

        return maxIndex;
    }
}
