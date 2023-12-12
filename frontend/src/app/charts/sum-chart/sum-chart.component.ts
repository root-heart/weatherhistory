import {Component, Input} from '@angular/core';
import {MinMaxSumDetails, SummarizedMeasurement, SummaryData} from "../../data-classes";
import {getDateLabel} from "../charts";


import {registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

import * as Highcharts from "highcharts";
import addMore from "highcharts/highcharts-more";
import {ChartComponentBase} from "../chart-component-base";
import {FilterService} from "../../filter.service";

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);

type MinMaxSumDetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends { sum?: number } ? K : never
}[keyof SummarizedMeasurement]


@Component({
    selector: 'sum-chart',
    templateUrl: './sum-chart.component.html',
    styleUrls: ['./sum-chart.component.css']
})
export class SumChartComponent extends ChartComponentBase {
    @Input() sumProperty: MinMaxSumDetailsProperty = "sunshineMinutes"
    @Input() sum2Property?: MinMaxSumDetailsProperty = undefined
    @Input() valueTooltipFormatter?: (originalValue: number) => string

    private sumSeries?: Highcharts.Series
    private sum2Series?: Highcharts.Series

    constructor(filterService: FilterService) {
        super(filterService)
    }

    protected override async setChartData(summaryData: SummaryData) {
        let sumData: Highcharts.PointOptionsType[] = []
        let sum2Data: number[][] = []

        if (summaryData.details) {
            summaryData.details.forEach(m => {
                let dateLabel = "dateInUtcMillis" in m ? m.dateInUtcMillis : getDateLabel(m)
                let measurements = m[this.sumProperty!]

                if (measurements.sum) {
                    sumData.push({
                        x: dateLabel,
                        y: measurements.sum,
                        custom: {
                            tooltipFormatter: this.valueTooltipFormatter
                        }
                    })
                }

                if (this.sum2Property && m[this.sum2Property!]) {
                    let sum2 = m[this.sum2Property!].sum
                    if (sum2) {
                        sum2Data.push([dateLabel, sum2])
                    }
                }
            })
        }

        this.sumSeries?.setData(sumData, false)
        this.sum2Series?.setData(sum2Data, false)
    }


    protected override createSeries(chart: Highcharts.Chart) {
        console.log('createSeries')

        this.sumSeries = chart.addSeries({
            type: "column",
            borderRadius: 0,
            stack: "s",
            stacking: "normal",
            yAxis: "yAxisSum",
            // TODO DRY
            tooltip: {valueSuffix: this.unit},
            name: this.name

        })

        if (this.sum2Property) {
            this.sum2Series = chart.addSeries({
                type: "column",
                borderRadius: 0,
                stack: "s",
                stacking: "normal",
                yAxis: "yAxisSum",
                // TODO DRY
                tooltip: {valueSuffix: this.unit},
                name: this.name
            })
        }
    }

    protected override getYAxes(): Highcharts.AxisOptions[] {
        console.log('getYAxes')
        console.log(this.yAxisLabelFormatter)
        return [{
            id: "yAxisSum",
            title: {text: undefined},
            reversedStacks: false,
            labels: {
                formatter: this.yAxisLabelFormatter
            }
        }]
    }

    // TODO DRY somehow
    protected override getTooltipText(_: Highcharts.Tooltip): string {
        // there is some unexplainable (at least to me) TypeScript/JavaScript magic happening here, where 'this' is an
        // object containing the members
        // color, colorIndex, key, percentage, point, series, total, x, y
        // beware: 'this' is not a reference to the enclosing class!!
        // @ts-ignore
        let tooltipInformation = this as TooltipInformation
        let point = tooltipInformation.point
        let series = tooltipInformation.series
        let date = new Date(point.x)
        let dateString = date.toLocaleDateString("de-DE", {day: "2-digit", month: "2-digit", year: "numeric"})
        let value = point.y
        if (point.custom?.tooltipFormatter) {
            value = point.custom.tooltipFormatter(value)
        }
        return `<b>${series.name}</b><br>`
            + `${dateString}: ${value} ${series.tooltipOptions.valueSuffix}`

    }
}
