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
import heatmap from "highcharts/modules/heatmap";

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
    @Input() heatmapColorStops?: { value: number, color: Highcharts.ColorString }[]

    private minMaxSeries?: Highcharts.Series
    private avgSeries?: Highcharts.Series

    constructor(filterService: FilterService) {
        super();
        filterService.currentData.subscribe(summaryData => {
            if (!this.property || !summaryData) {
                return
            }

            this.chart?.showLoading("Aktualisiere Diagramm...")
            let minMaxData: number[][] = []
            let avgData: number[][] = []
            if (summaryData.details) {
                summaryData.details.forEach(m => {
                    let dateLabel = "dateInUtcMillis" in m ? m.dateInUtcMillis : getDateLabel(m)
                    let measurements = m[this.property!]

                    minMaxData.push([dateLabel, "min" in measurements ? measurements.min : 0, measurements.max])
                    avgData.push([dateLabel, measurements.avg])
                })
            }

            this.minMaxSeries?.setData(minMaxData, false)
            this.avgSeries?.setData(avgData, false)

            this.chart?.redraw()

            this.chart?.hideLoading()
        })
    }

    protected override createSeries(chart: Highcharts.Chart) {
        this.minMaxSeries = chart.addSeries({
            type: 'arearange',
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}},
            yAxis: "yAxisMinAvgMax",
        })
        this.avgSeries = chart.addSeries({
            type: 'line',
            marker: {enabled: false},
            yAxis: "yAxisMinAvgMax",
        })
    }

    protected override getYAxes(): Highcharts.AxisOptions[] {
        return [{
            id: "yAxisMinAvgMax",
            title: {text: undefined},
            reversedStacks: false,
            offset: 0
        }]
    }
}
