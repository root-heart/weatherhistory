import {Component, Input} from '@angular/core';
import {Observable} from "rxjs";
import {MinMaxSumDetails, SummarizedMeasurement, SummaryData} from "../../data-classes";
import {getDateLabel} from "../charts";


import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {registerLocaleData} from "@angular/common";

import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);


export type Sum = {
    dateLabel: number,
    sum: number
}

type MinMaxSumDetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends MinMaxSumDetails ? K : never
}[keyof SummarizedMeasurement]


@Component({
    selector: 'sum-chart',
    templateUrl: './sum-chart.component.html',
    styleUrls: ['./sum-chart.component.css']
})
export class SumChartComponent {
    @Input() sum: MinMaxSumDetailsProperty = "sunshineMinutes"
    Highcharts: typeof Highcharts = Highcharts;
    chart?: Highcharts.Chart;
    chartCallback: Highcharts.ChartCallbackFunction = c => this.chart = c
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


    @Input() set dataSource(c: Observable<SummaryData | undefined>) {
        c.subscribe(summaryData => {
            if (summaryData && this.chart) {
                console.log('set sum data')
                let minAvgMaxData = summaryData.details
                    .map(m => {
                        if (this.sum) {
                            let s = m.measurements![this.sum].sum
                            return {dateLabel: getDateLabel(m), sum: s}
                        } else {
                            return null
                        }
                    })
                    .filter(d => d !== null && d !== undefined)
                    .map(d => d!)
                while (this.chart.series.length > 0) {
                    this.chart.series[0].remove()
                }
                this.chart.addSeries({
                    type: "column",
                    data: minAvgMaxData.map(d => [d.dateLabel, d.sum]),
                    borderRadius: 0
                })
            }
        })
    }


}
