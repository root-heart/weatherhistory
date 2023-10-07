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
    sum?: number
    sum2?: number
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
    @Input() sumProperty: MinMaxSumDetailsProperty = "sunshineMinutes"
    @Input() sum2Property?: MinMaxSumDetailsProperty = undefined
    Highcharts: typeof Highcharts = Highcharts;
    chart?: Highcharts.Chart;
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
            if (!this.chart || !this.sumProperty) return

            while (this.chart.series.length > 0) {
                this.chart.series[0].remove()
            }
            if (summaryData) {
                let minAvgMaxData = summaryData.details
                    .map(m => {
                        let record: Sum = {
                            dateLabel: getDateLabel(m),
                            sum: m.measurements![this.sumProperty].sum
                        }
                        if (this.sum2Property) {
                            record.sum2 = m.measurements![this.sum2Property].sum
                        }
                        return record
                    })
                    .filter(d => d !== null && d !== undefined)
                    .map(d => d!)

                this.chart.addSeries({
                    type: "column",
                    data: minAvgMaxData.map(d => [d.dateLabel, d.sum]),
                    borderRadius: 0
                })
                if (this.sum2Property) {
                    this.chart.addSeries({
                        type: "column",
                        data: minAvgMaxData.map(d => [d.dateLabel, d.sum2]),
                        borderRadius: 0
                    })
                }
            }
        })
    }

    chartCallback: Highcharts.ChartCallbackFunction = c => this.chart = c


}
