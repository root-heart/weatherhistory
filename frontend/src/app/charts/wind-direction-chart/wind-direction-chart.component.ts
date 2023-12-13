import {Component, ViewEncapsulation} from '@angular/core';
import {getDateLabel} from "../charts";


import {registerLocaleData} from "@angular/common";
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {ChartComponentBase} from "../chart-component-base";
import {FilterService} from "../../filter.service";
import _ from 'lodash';
import heatmap from 'highcharts/modules/heatmap';
import {SummaryData} from "../../data-classes";
import {FetchMeasurementsService} from "../../services/fetch-measurements.service";


addMore(Highcharts);
heatmap(Highcharts)
registerLocaleData(localeDe, 'de-DE', localeDeExtra);

@Component({
    selector: 'wind-direction-chart',
    templateUrl: './wind-direction-chart.component.html',
    styleUrls: ['./wind-direction-chart.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class WindDirectionChart extends ChartComponentBase {
    // TODO DRY - use heatmap chart somehow
    windDirectionChartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false, zooming: {mouseWheel: {enabled: true}, type: "x"}},
        colorAxis: {min: 0, minColor: 'rgb(70, 50, 80)', maxColor: 'rgb(210, 150, 240)'},
        legend: {enabled: false},
        title: {text: undefined},
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
        series: this.createSeries(),
        xAxis: {
            id: "xAxis",
            crosshair: true,
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {day: "numeric", month: "short"})
            },
        },
        yAxis: {
            id: "yAxis",
            title: {text: undefined},
            min: 0,
            max: 360,
            tickInterval: 45,
            gridLineWidth: 0,
            labels: {
                formatter: c => {
                    switch (c.value) {
                        case 0:
                            return "N"
                        case 45:
                            return "NO"
                        case  90:
                            return "O"
                        case  135:
                            return "SO"
                        case  180:
                            return "S"
                        case  225:
                            return "SW"
                        case  270:
                            return "W"
                        case  315:
                            return "NW"
                        case  360:
                            return "N"
                    }
                    return "?"
                }
            },
            plotBands: [{
                className: "north",
                from: -5,
                to: 45
            }, {
                className: "east",
                from: 45,
                to: 135
            }, {
                className: "south",
                from: 135,
                to: 225
            }, {
                className: "west",
                from: 225,
                to: 315
            }, {
                className: "north",
                from: 315,
                to: 365
            }],
        },
    }

    constructor(fetchMeasurementsService: FetchMeasurementsService) {
        super(fetchMeasurementsService);
    }

    protected override async setChartData(summaryData: SummaryData): Promise<void> {
        let scatterData: Highcharts.PointOptionsType[] = []
        summaryData.details.forEach(m => {
            let dateLabel = "dateInUtcMillis" in m ? m.dateInUtcMillis : getDateLabel(m)
            let counted: _.Dictionary<number> = _.countBy(m.windDirectionDegrees.details);
            _.each(counted, (count, directionString) => {
                scatterData.push([dateLabel, parseInt(directionString), count])
            })
        })
        this.chart?.series[0]?.setData(scatterData)
    }

    protected createSeries(): Highcharts.SeriesOptionsType[] {
        return [{
            type: 'heatmap',
            colsize: 24 * 60 * 60 * 1000,
            rowsize: 10,
            turboThreshold: 0,
            // enableMouseTracking: false,
            className: "windDirection"
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
        console.log(tooltipInformation)
        let date = new Date(point.x)
        let dateString = date.toLocaleDateString("de-DE", {day: "2-digit", month: "2-digit", year: "numeric"})
        return `<b>${series.name}</b><br>`
            + `${dateString}: ${point.y} ${series.tooltipOptions.valueSuffix}`

    }
}
