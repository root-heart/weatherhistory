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
import {FetchMeasurementsService} from "../../services/fetch-measurements.service";

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

    constructor(fetchMeasurementsService: FetchMeasurementsService) {
        super(fetchMeasurementsService);
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
        this.chart?.series[0]?.setData(sumData, false)
        this.chart?.series[1]?.setData(sum2Data, false)
    }

    protected override createSeries(): Highcharts.SeriesOptionsType[] {
        return [{
            type: "column",
            borderRadius: 0,
            stack: "s",
            stacking: "normal"
        }, {
            type: "column",
            borderRadius: 0,
            stack: "s",
            stacking: "normal"
        }]
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
