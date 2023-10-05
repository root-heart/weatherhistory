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
    sunshineDurationChart?: Highcharts.Chart;
    cloudinessChart?: Highcharts.Chart;

    chartCallback: Highcharts.ChartCallbackFunction;
    sunshineDurationChartCallback: Highcharts.ChartCallbackFunction;
    cloudinessChartCallback: Highcharts.ChartCallbackFunction;

    temperatureChartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false},
        title: {text: undefined},
        yAxis: [{title: {text: undefined}}],
        tooltip: {shared: true},
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false}
        }
    }

    sunshineDurationChartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false},
        title: {text: undefined},
        yAxis: [{title: {text: undefined}}],
        tooltip: {shared: true},
        plotOptions: {
            column: {animation: false}
        }
    }

    cloudinessChartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false},
        title: {text: undefined},
        yAxis: [{title: {text: undefined}, reversed: true}],
        tooltip: {shared: true},
        plotOptions: {
            column: {animation: false}
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
        this.chartCallback = c => this.temperatureChart = c;
        this.sunshineDurationChartCallback = c => this.sunshineDurationChart = c
        this.cloudinessChartCallback = c => this.cloudinessChart = c

        filterService.currentData.subscribe(data => {
            if (data) {
                if (this.temperatureChart) {
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
                        lineWidth: 3,
                        marker: { enabled: false },
                        states: { hover: { enabled: false } }
                    })
                    this.temperatureChart.addSeries({
                        type: 'line',
                        data: avg,
                        marker: { enabled: false }
                    })
                }

                if (this.sunshineDurationChart) {
                    let sunshineDurations = data.details.map(m => m.measurements)
                        .filter(m => m)
                        .map(m => m!.sunshineMinutes)
                        .filter(s => s)
                    while (this.sunshineDurationChart.series.length > 0) {
                        this.sunshineDurationChart.series[0].remove()
                    }
                    this.sunshineDurationChart.addSeries({
                        type: "column",
                        data: sunshineDurations.map(s => s.sum! / 60),
                        pointPadding: 0,
                        groupPadding: 0,
                        borderRadius: 0,
                        borderWidth: 0
                    })
                }

                if (this.cloudinessChart) {
                    let cloudiness = data.details.map(m => m.measurements)
                        .filter(m => m)
                        .map(m => m?.cloudCoverageHistogram)
                        .filter(h => h)
                    while (this.cloudinessChart.series.length > 0) {
                        this.cloudinessChart.series[0].remove()
                    }
                    for (let i = 0; i <= 8; i++) {
                        this.cloudinessChart.addSeries({
                            type: "column",
                            data: cloudiness.map(c => c![i]),
                            stack: 's',
                            stacking: 'percent',
                            pointPadding: 0,
                            groupPadding: 0,
                            borderRadius: 0,
                            borderWidth: 0
                        })
                    }
                }
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
