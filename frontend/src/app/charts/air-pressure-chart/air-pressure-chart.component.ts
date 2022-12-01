import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord, MeasurementDataSet} from "../BaseChart";

export type AirPressureRecord = BaseRecord & {
    minAirPressureHectopascals: number,
    avgAirPressureHectopascals: number,
    maxAirPressureHectopascals: number,
}

@Component({
    selector: 'air-pressure-chart',
    template: '<canvas #airPressureChart></canvas>',
    styleUrls: ['../charts.css']
})
export class AirPressureChart extends BaseChart<AirPressureRecord> implements OnInit {
    @ViewChild("airPressureChart")
    private canvas?: ElementRef;


    constructor() {
        super();
        this.includeZero = false
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: Array<AirPressureRecord>): Array<MeasurementDataSet> {
        return [{
            type: 'line',
            label: 'Luftdruck',
            borderColor: 'hsl(120, 80%, 45%)',
            borderWidth: 2,
            // backgroundColor: 'hsla(120, 80%, 45%, 0)',
            data: summaryList.map(m => m.avgAirPressureHectopascals),
            pointRadius: 0,
            pointHitRadius: 20,
            showTooltip: true,
            showLegend: true,
            // tooltipValueFormatter: (value: number) => this.formatCentigrade(value)
        }, {
            type: 'line',
            label: 'min Luftdruck',
            borderWidth: 0,
            borderColor: 'hsla(120, 80%, 45%, 0)',
            backgroundColor: 'hsla(120, 80%, 45%, 0.15)',
            data: summaryList.map(m => m.minAirPressureHectopascals),
            showTooltip: true,
            // xAxisID: "x2"
        }, {
            type: 'line',
            // stepped: true,
            label: 'max Luftdruck',
            borderColor: 'hsla(120, 80%, 45%, 0)',
            backgroundColor: 'hsla(120, 80%, 45%, 0.15)',
            data: summaryList.map(m => m.maxAirPressureHectopascals),
            showTooltip: true,
            fill: "-1"
        }]
    }

}
