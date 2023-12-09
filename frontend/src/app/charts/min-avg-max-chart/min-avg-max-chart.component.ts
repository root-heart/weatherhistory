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
    private detailedSeries?: Highcharts.Series

    constructor(filterService: FilterService) {
        super();
        filterService.currentData.subscribe(summaryData => {
            if (!this.property || !summaryData) {
                return
            }

            this.chart?.showLoading("Aktualisiere Diagramm...")
            let minMaxData: number[][] = []
            let avgData: number[][] = []
            let heatmapData: Highcharts.PointOptionsType[] = []
            if (summaryData.details) {
                summaryData.details.forEach(m => {
                    let dateLabel = "dateInUtcMillis" in m ? m.dateInUtcMillis : getDateLabel(m)
                    let measurements = m[this.property!]

                    minMaxData.push([dateLabel, "min" in measurements ? measurements.min : 0, measurements.max])
                    avgData.push([dateLabel, measurements.avg])
                    let details = measurements.details
                    if (details) {
                        for (let hour = 0; hour < details.length; hour++) {
                            let value = details[hour]
                            if (value != null) {
                                heatmapData.push([dateLabel, hour, details[hour]])
                            }
                        }
                    }
                })
            }

            this.minMaxSeries?.setData(minMaxData, false)
            this.avgSeries?.setData(avgData, false)
            this.detailedSeries?.setData(heatmapData, false)

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
        this.detailedSeries = chart.addSeries({
            type: 'heatmap',
            colsize: 24 * 60 * 60 * 1000,
            turboThreshold: 0,
            yAxis: "yAxisDetails",
        })
    }

    protected override getYAxes(): Highcharts.AxisOptions[] {
        return [{
            id: "yAxisMinAvgMax",
            title: {text: undefined},
            reversedStacks: false,
            height: "48%",
            offset: 0
        }, {
            id: "yAxisDetails",
            title: {text: undefined},
            reversedStacks: false,
            min: 0,
            max: 23,
            tickInterval: 6,
            endOnTick: false,
            top: "52%",
            height: "48%",
            offset: 0
        }]
    }

    protected override getColorAxis(): Highcharts.ColorAxisOptions | null {
        if (this.heatmapColorStops) {
            let minStopValue = Math.min.apply(null, this.heatmapColorStops.map(s => s.value))
            let maxStopValue = Math.max.apply(null, this.heatmapColorStops.map(s => s.value))
            let heatmapColorStopsRelative: [number, Highcharts.ColorString][] = this.heatmapColorStops
                .map(s => [(s.value - minStopValue) / (maxStopValue - minStopValue), s.color])

            return {stops: heatmapColorStopsRelative}
        } else {
            return {minColor: 'rgb(70, 50, 80)', maxColor: 'rgb(210, 150, 240)'}
        }
    }
}
