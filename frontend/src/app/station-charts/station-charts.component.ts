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

    temperatureChart?: Highcharts.Chart;
    sunshineDurationChart?: Highcharts.Chart;
    cloudinessChart?: Highcharts.Chart;
    precipitationChart?: Highcharts.Chart;

    chartCallback: Highcharts.ChartCallbackFunction;
    sunshineDurationChartCallback: Highcharts.ChartCallbackFunction;
    cloudinessChartCallback: Highcharts.ChartCallbackFunction;
    precipitationChartCallback: Highcharts.ChartCallbackFunction;

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
        this.chartCallback = c => this.temperatureChart = c;
        this.sunshineDurationChartCallback = c => this.sunshineDurationChart = c
        this.cloudinessChartCallback = c => this.cloudinessChart = c
        this.precipitationChartCallback = c => this.precipitationChart = c

        filterService.currentData.subscribe(data => {
            if (data) {
                if (this.temperatureChart) {
                    let airTemps = data.details
                        .map(m => (
                            {date: getDateLabel(m), temps: m.measurements?.airTemperatureCentigrade}
                        ));
                    while (this.temperatureChart.series.length > 0) {
                        this.temperatureChart.series[0].remove()
                    }
                    this.temperatureChart.addSeries({
                        type: 'arearange',
                        data: airTemps.map(t =>
                            [t.date, t.temps?.min, t.temps?.max]
                        ),
                        lineWidth: 3,
                        marker: {enabled: false},
                        states: {hover: {enabled: false}}
                    })
                    this.temperatureChart.addSeries({
                        type: 'line',
                        data: airTemps.map(t =>
                            [t.date, t.temps?.avg]
                        ),
                        marker: {enabled: false}
                    })
                }

                if (this.sunshineDurationChart) {
                    let sunshineDurations = data.details
                        .map(m => (
                            {date: getDateLabel(m), sunshineDurations: m.measurements!.sunshineMinutes}
                        ));
                    while (this.sunshineDurationChart.series.length > 0) {
                        this.sunshineDurationChart.series[0].remove()
                    }
                    this.sunshineDurationChart.addSeries({
                        type: "column",
                        data: sunshineDurations.map(s =>
                            [s.date, s.sunshineDurations.sum! / 60]
                        ),
                        borderRadius: 0
                    })
                }

                if (this.precipitationChart) {
                    let sunshineDurations = data.details
                        .map(m => (
                            {date: getDateLabel(m), rain: m.measurements!.rainfallMillimeters, snow: m.measurements!.snowfallMillimeters}
                        ));
                    while (this.precipitationChart.series.length > 0) {
                        this.precipitationChart.series[0].remove()
                    }
                    this.precipitationChart.addSeries({
                        type: "column",
                        data: sunshineDurations.map(s =>
                            [s.date, s.rain.sum!]
                        ),
                        borderRadius: 0
                    })
                    this.precipitationChart.addSeries({
                        type: "column",
                        data: sunshineDurations.map(s =>
                            [s.date, s.snow.sum!]
                        ),
                        borderRadius: 0
                    })
                }

                if (this.cloudinessChart) {
                    let cloudiness = data.details
                        .map(m => (
                            {date: getDateLabel(m), cloudiness: m.measurements?.cloudCoverageHistogram}
                        ));
                    while (this.cloudinessChart.series.length > 0) {
                        this.cloudinessChart.series[0].remove()
                    }
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
                            borderRadius: 0
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
