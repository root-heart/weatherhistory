import {Component, Input} from '@angular/core';
import {AvgMaxDetails, MinAvgMaxDetails, SummarizedMeasurement, SummaryData} from "../../data-classes";
import {getDateLabel} from "../charts";


import {registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {ChartComponentBase} from "../chart-component-base";
import {FilterService} from "../../filter.service";

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);

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
export class MinAvgMaxChart extends ChartComponentBase {
    @Input() property?: MinAvgMaxDetailsProperty | AvgMaxDetailsProperty

    private minMaxSeries?: Highcharts.Series
    private avgSeries?: Highcharts.Series

    constructor(filterService: FilterService) {
        super();
        filterService.currentData.subscribe(summaryData => {
            if (!this.property) {
                return
            }
            if (summaryData) {
                this.minMaxSeries?.setData(summaryData.details.map(m => {
                    let measurement = m.measurements![this.property!]
                    return [
                        getDateLabel(m), "min" in measurement ? measurement.min : 0, measurement.max
                    ]
                }))
                this.avgSeries?.setData(summaryData.details.map(m => [
                    getDateLabel(m), m.measurements![this.property!].avg
                ]))
            }
        })
    }

    protected override createSeries(chart: Highcharts.Chart) {
        this.minMaxSeries = chart.addSeries({
            type: 'arearange',
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}}
        })
        this.avgSeries = chart.addSeries({
            type: 'line',
            marker: {enabled: false}
        })
    }
}
