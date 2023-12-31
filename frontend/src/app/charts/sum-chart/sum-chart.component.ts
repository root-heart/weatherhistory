import {Component, Input} from '@angular/core';
import {registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

import * as Highcharts from "highcharts";
import addMore from "highcharts/highcharts-more";
import {ChartBaseComponent} from "../chart-base.component";

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);

@Component({
    selector: 'sum-chart',
    template: `
        <highcharts-chart [Highcharts]='Highcharts' [options]='chartOptions' [callbackFunction]='chartCallback'/>`,
    styles: [`
        highcharts-chart {
            display: block;
            height: 100%;
        }`]
})
export class SumChartComponent extends ChartBaseComponent<[number, number]> {
    @Input() valueTooltipFormatter?: (originalValue: number) => string

    protected override async setChartData(data: [number, number][]) {
        let sumData = data.map(d => ({
            x: d[0],
            y: d[1],
            custom: {
                tooltipFormatter: this.valueTooltipFormatter
            }
        }))

        this.chart?.series[0]?.setData(sumData, false)
    }

    protected override createSeries(): Highcharts.SeriesOptionsType[] {
        return [{
            type: "column",
            borderRadius: 0
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
