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
export class StationChartsComponent {

    Highcharts: typeof Highcharts = Highcharts;
    cloudinessChart?: Highcharts.Chart;
    windDirectionChart?: Highcharts.Chart;
    chartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false, zooming: {mouseWheel: {enabled: true}, type: "x"}},
        boost: {
            useGPUTranslations: true,
            // usePreAllocated: true
        },
        title: {text: undefined},
        xAxis: {
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {month: "short"})
            },
        },
        yAxis: [
            {title: {text: undefined}, reversedStacks: false},
        ],
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false},
            column: {animation: false},
            scatter: {marker: {symbol: 'circle'}}
        }
    }

    windDirectionChartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false, zooming: {mouseWheel: {enabled: true}, type: "x"}},
        boost: {
            useGPUTranslations: true,
            // usePreAllocated: true
        },
        title: {text: undefined},
        xAxis: {
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {month: "short"})
            },
        },
        yAxis: [
            {title: {text: undefined}, min: 0, max: 360, tickInterval: 90, minorTickInterval: 45}
        ],
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false},
            column: {animation: false},
            scatter: {marker: {symbol: 'circle'}}
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
        // TODO move wind direction chart related stuff into own component
        filterService.currentData.subscribe(data => {
            if (data) {
                if (this.windDirectionChart) {
                    this.clearChart(this.windDirectionChart)

                    let scatterData = data.details
                        .map(m => ({
                            date: getDateLabel(m),
                            hourlyWindSpeeds: m.measurements?.windDirectionDegrees
                        }))

                    let s1 = []
                    for (let sd of scatterData) {
                        if (sd.hourlyWindSpeeds) {
                            let min = sd.hourlyWindSpeeds.min
                            let max = sd.hourlyWindSpeeds.max
                            if (max > min) {
                                s1.push([sd.date, min, max])
                            } else {
                                s1.push([sd.date, min, 360])
                                s1.push([sd.date, 0, max])
                            }
                        }
                    }

                    this.windDirectionChart.addSeries({
                        type: "columnrange",
                        data: s1,
                        grouping: false
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

    cloudinessChartCallback: Highcharts.ChartCallbackFunction = c => this.cloudinessChart = c;

    windDirectionChartCallback: Highcharts.ChartCallbackFunction = c => this.windDirectionChart = c;

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
