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

// TODO make me be a heatmap chart instead
@Component({
    selector: 'cloud-coverage-chart',
    templateUrl: './cloud-coverage-chart.component.html',
    styleUrls: ['./cloud-coverage-chart.component.css']
})
export class CloudCoverageChart extends ChartComponentBase {
    cloudCoverageChartOptions: Highcharts.Options = {
        chart: {
            styledMode: true,
            animation: false,
            spacing: [0, 0, 0, 0],
        },
        colorAxis: {
            min: 0, minColor: "#a3ccf5", max: 8, maxColor: "#78786d",
            stops: [
                [-1, "#400"],
                [0, "#a3ccf5"],
                [1 / 8, "#e7f2fe"],
                [2 / 8, "#faf7d1"],
                [3 / 8, "#ede8ab"],
                [4 / 8, "#d5cf90"],
                [5 / 8, "#c6c29f"],
                [6 / 8, "#aaa9a1"],
                [7 / 8, "#929187"],
                [8 / 8, "#78786d"],
                [9 / 8, "#5e0d05"]
            ]
        },
        legend: {enabled: false},
        title: {text: undefined},
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
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
            max: 23,
            endOnTick: false,
            tickInterval: 6,
        },
    }

    private cloudCoverageSeries?: Highcharts.Series;

    constructor(filterService: FilterService) {
        super();
        filterService.currentData.subscribe(data => {
            if (!data) {
                return
            }

            let scatterData: Highcharts.PointOptionsType[] = []
            data.details.forEach(m => {
                let dateLabel = "dateInUtcMillis" in m ? m.dateInUtcMillis : getDateLabel(m)
                let hours = m.detailedCloudCoverage?.length || 0;
                for (let hour = 0; hour < hours; hour++) {
                    let cloudCoverage = m.detailedCloudCoverage[hour];
                    if (cloudCoverage != null) {
                        scatterData.push([dateLabel, hour, cloudCoverage])
                    }
                }
            })

            this.cloudCoverageSeries!.setData(scatterData)
        })
    }

    protected createSeries(chart: Highcharts.Chart): void {
        this.cloudCoverageSeries = chart.addSeries({
            type: 'heatmap',
            colsize: 24 * 60 * 60 * 1000,
            rowsize: 1,
            turboThreshold: 0,
            className: "cloudCoverage"
        })
    }

}
