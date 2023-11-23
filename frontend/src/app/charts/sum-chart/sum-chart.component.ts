import {Component, Input} from '@angular/core';
import {Observable} from "rxjs";
import {MinMaxSumDetails, SummarizedMeasurement, SummaryData} from "../../data-classes";
import {getDateLabel} from "../charts";


import {registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

import {ChartComponentBase} from "../chart-component-base";

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
export class SumChartComponent extends ChartComponentBase {
    @Input() sumProperty: MinMaxSumDetailsProperty = "sunshineMinutes"
    @Input() sum2Property?: MinMaxSumDetailsProperty = undefined

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
                    borderRadius: 0,
                    stack: "s",
                    stacking: "normal"
                })
                if (this.sum2Property) {
                    this.chart.addSeries({
                        type: "column",
                        data: minAvgMaxData.map(d => [d.dateLabel, d.sum2]),
                        borderRadius: 0,
                        stack: "s",
                        stacking: "normal"
                    })
                }
            }
        })
    }
}
