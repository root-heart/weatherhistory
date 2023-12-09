import {Component, Input} from '@angular/core';
import {MinMaxSumDetails, SummarizedMeasurement} from "../../data-classes";
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
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends MinMaxSumDetails ? K : never
}[keyof SummarizedMeasurement]


@Component({
    selector: 'sum-chart',
    templateUrl: './sum-chart.component.html',
    styleUrls: ['./sum-chart.component.css']
})
export class SumChartComponent extends ChartComponentBase {
    @Input() sumProperty: MinMaxSumDetailsProperty = "sunshineMinutes"
    @Input() sum2Property?: MinMaxSumDetailsProperty = undefined
    private sumSeries?: Highcharts.Series
    private sum2Series?: Highcharts.Series

    constructor(filterService: FilterService) {
        super()
        filterService.currentData.subscribe(summaryData => {
            if (summaryData) {
                this.sumSeries?.setData(summaryData.details.map(m => [
                    getDateLabel(m), m.measurements![this.sumProperty].sum
                ]))
                if (this.sum2Property) {
                    this.sum2Series?.setData(summaryData.details.map(m => [
                        getDateLabel(m), m.measurements![this.sum2Property!].sum
                    ]))
                }
            }
        })

    }


    protected override createSeries(chart: Highcharts.Chart) {
        this.sumSeries = chart.addSeries({
            type: "column",
            borderRadius: 0,
            stack: "s",
            stacking: "normal"
        })
        if (this.sum2Property) {
            this.sum2Series = chart.addSeries({
                type: "column",
                borderRadius: 0,
                stack: "s",
                stacking: "normal"
            })
        }
    }
}
