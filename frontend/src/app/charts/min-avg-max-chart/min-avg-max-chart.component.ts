import {Component} from '@angular/core';

import {registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {ChartBaseComponent} from "../chart-base.component";

addMore(Highcharts);
registerLocaleData(localeDe, 'de-DE', localeDeExtra);

@Component({
    selector: 'min-avg-max-chart',
    template: `
        <highcharts-chart [Highcharts]='Highcharts' [options]='chartOptions' [callbackFunction]='chartCallback'/>`,
    styles: [`
        highcharts-chart {
            display: block;
            height: 100%;
        }`]
})
export class MinAvgMaxChart extends ChartBaseComponent<[number, number, number, number]> {
    protected override async setChartData(data: [number, number, number, number][]): Promise<void> {
        let minMaxData: [number, number, number][] = []
        let avgData: [number, number][] = []

            data.forEach(d => {
                let dateLabel = d[0]
                minMaxData.push([dateLabel, d[1], d[3]])
                avgData.push([dateLabel, d[2]])
            })

        this.chart?.series[0]?.setData(minMaxData, false)
        this.chart?.series[1]?.setData(avgData, false)
    }

    protected override createSeries(): Highcharts.SeriesOptionsType[] {
        return [{
            type: 'arearange',
            lineWidth: 3,
            marker: {enabled: false},
            states: {hover: {enabled: false}},
        }, {
            type: 'line',
            marker: {enabled: false},
            yAxis: "yAxisMinAvgMax",

        }]
    }

    protected override getYAxes(): Highcharts.AxisOptions[] {
        return [{
            id: "yAxisMinAvgMax",
            title: {text: undefined},
            reversedStacks: false,
            offset: 0
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
        let points = tooltipInformation.points
        let series = points[0].series
        let date = new Date(points[0].x)
        let dateString = date.toLocaleDateString("de-DE", {day: "2-digit", month: "2-digit", year: "numeric"})
        let unit = series.tooltipOptions.valueSuffix;
        return `<b>${series.name}</b><br>`
            + `Datum: ${dateString}<br>`
            + `Höchstwert: ${points[0].point.high} ${unit}<br>`
            + `Durchschnitt: ${points[1].point.y} ${unit}<br>`
            + `Tiefstwert: ${points[0].point.y} ${unit}`
    }
}
