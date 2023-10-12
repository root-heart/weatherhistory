import {Component, Input} from '@angular/core';
import {AvgMaxDetails, ChartResolution, MinAvgMaxDetails, SummarizedMeasurement, SummaryData} from "../../data-classes";


import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {registerLocaleData} from "@angular/common";

import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';
import {Observable} from "rxjs";
import {getDateLabel} from "../charts";
import {ChartComponentBase} from "../chart-component-base";

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);


type MinAvgMaxSummary = {
    dateLabel: number,
    min: number,
    avg: number,
    max: number,
}


type MinAvgMaxDetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends MinAvgMaxDetails ? K : never
}[keyof SummarizedMeasurement]

type AvgMaxDetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends AvgMaxDetails ? K : never
}[keyof SummarizedMeasurement]


@Component({
  selector: 'min-avg-max-chart',
  templateUrl: './min-avg-max-chart.component.html',
  styleUrls: ['./min-avg-max-chart.component.css']
})
export class MinAvgMaxChart extends ChartComponentBase{
    @Input() property?: MinAvgMaxDetailsProperty | AvgMaxDetailsProperty
    @Input() set dataSource(c: Observable<SummaryData | undefined>) {
        c.subscribe(summaryData => {
            if (!this.property) {
                return
            }
            if (summaryData) {
                // TODO this is duplicated in the other chart classes
                let minAvgMaxData: MinAvgMaxSummary[] = summaryData.details
                    .map(m => {
                        let measurement = m.measurements![this.property!]
                        return {
                            dateLabel: getDateLabel(m),
                            min: "min" in measurement ? measurement.min : 0,
                            avg: measurement.avg,
                            max: measurement.max
                        }
                    })
                    .filter(d => d !== null && d !== undefined)
                    .map(d => d!)
                this.setData(minAvgMaxData, summaryData.resolution)
            } else {
                this.setData([], "month")
            }
        })
    }

    public setData(data: Array<MinAvgMaxSummary>, resolution: ChartResolution): void {
        if (!this.chart) {
            return
        }

        while (this.chart.series.length > 0) {
            this.chart.series[0].remove()
        }

        this.chart.addSeries({
            type: 'arearange',
            data: data.map(t =>
                [t.dateLabel, t.min, t.max]
            ),
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}}
        })
        this.chart.addSeries({
            type: 'line',
            data: data.map(t =>
                [t.dateLabel, t.avg]
            ),
            marker: {enabled: false}
        })
    }

}
