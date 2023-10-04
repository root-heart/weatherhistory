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

addMore(Highcharts);

@Component({
    selector: 'station-charts',
    templateUrl: './station-charts.component.html',
    styleUrls: ['./station-charts.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class StationChartsComponent implements OnInit {

    updateFlag = false;

    data = [1, 2, 3, 4];

    Highcharts: typeof Highcharts = Highcharts;
    temperatureChart?: Highcharts.Chart;
    chartCallback: Highcharts.ChartCallbackFunction;

    temperatureChartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false},
        yAxis: [{title: {text: 'Air Temperature'}}],
        tooltip: {shared: true},
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false}
        }
    }

    faSun = faSun
    faCloudSun = faCloudSun
    faCloud = faCloud
    faRain = faCloudShowersHeavy
    faSnow = faSnowflake
    faSquare = faSquare
    faSquareChecked = faSquareXmark
    faCalendar = faCalendarWeek
    measurementType?: MeasurementTypes

    constructor(public filterService: FilterService) {
        this.chartCallback = (c: any) => {
            this.temperatureChart = c;
        };

        filterService.currentData.subscribe(data => {
            if (data && this.temperatureChart) {
                let airTemps = data.details
                    .map(m => m.measurements)
                    .filter(m => m)
                    .map(m => m!.airTemperatureCentigrade)
                    .filter(t => t)
                let minMax = airTemps
                    .map(t => [t!.min, t!.max]);
                let avg = airTemps.map(t => t.avg)
                while (this.temperatureChart.series.length > 0) {
                    this.temperatureChart.series[0].remove()
                }
                this.temperatureChart.addSeries({
                    type: 'arearange',
                    data: minMax,
                    lineWidth: 3
                })
                this.temperatureChart.addSeries({
                    type: 'line',
                    data: avg
                })
            }
        })
    }

    ngOnInit(): void {

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

}
