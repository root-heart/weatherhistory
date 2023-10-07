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
    styleUrls: ['./station-charts.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class StationChartsComponent implements OnInit {

    updateFlag = false;

    data = [1, 2, 3, 4];

    Highcharts: typeof Highcharts = Highcharts;

    sunshineDurationChart?: Highcharts.Chart;
    cloudinessChart?: Highcharts.Chart;
    windDirectionChart?: Highcharts.Chart;

    sunshineDurationChartCallback: Highcharts.ChartCallbackFunction;
    cloudinessChartCallback: Highcharts.ChartCallbackFunction;
    windDirectionChartCallback: Highcharts.ChartCallbackFunction;

    chartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false, zooming: {mouseWheel: {enabled: true}, type: "x"}},
        title: {text: undefined},
        xAxis: {
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {month: "short"})
            },
        },
        yAxis: [{title: {text: undefined}, reversedStacks: false}],
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y"
        },
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false},
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
        this.sunshineDurationChartCallback = c => this.sunshineDurationChart = c
        this.cloudinessChartCallback = c => this.cloudinessChart = c
        this.windDirectionChartCallback = c => this.windDirectionChart = c
        filterService.currentData.subscribe(data => {
            if (data) {
                if (this.sunshineDurationChart) {
                    let sunshineDurations = data.details
                        .map(m => (
                            {date: getDateLabel(m), sunshineDurations: m.measurements!.sunshineMinutes}
                        ));
                    this.clearChart(this.sunshineDurationChart)
                    this.sunshineDurationChart.addSeries({
                        type: "column",
                        data: sunshineDurations.map(s =>
                            [s.date, s.sunshineDurations.sum! / 60]
                        ),
                        borderRadius: 0
                    })
                }

                if (this.windDirectionChart) {
                    let windDirection = data.details
                        .map(m => (
                            {date: getDateLabel(m), directions: m.measurements?.detailedWindDirectionDegrees}
                        ));
                    this.clearChart(this.windDirectionChart)
                    this.windDirectionChart.addSeries({
                        type: "scatter",
                        data: windDirection.map(d => [d.date, d.directions])
                    })
                }

                if (this.cloudinessChart) {
                    let cloudiness = data.details
                        .map(m => (
                            {date: getDateLabel(m), cloudiness: m.measurements?.cloudCoverageHistogram}
                        ));
                    this.clearChart(this.cloudinessChart)
                    for (let i = 0; i <= 8; i++) {
                        this.cloudinessChart.addSeries({
                            type: "column",
                            data: cloudiness.map(c =>
                                [c.date, c.cloudiness![i]]
                            ),
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

    private clearChart(chart: Highcharts.Chart) {
        while (chart.series.length > 0) {
            chart.series[0].remove()
        }
    }

}
