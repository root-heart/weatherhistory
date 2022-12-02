import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord, MeasurementDataSet, MinAvgMaxChart} from "../BaseChart";

@Component({
    selector: 'temperature-chart',
    template: '<canvas #temperatureChart></canvas>',
    styleUrls: ['../charts.css']
})
export class TemperatureChart extends BaseChart<TemperatureRecord> implements OnInit {
    @ViewChild("temperatureChart")
    private canvas?: ElementRef;

    constructor() {
        super();
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: Array<TemperatureRecord>): Array<MeasurementDataSet> {
        return [{
            type: "line",
            label: 'Temperatur',
            borderWidth: 2,
            borderColor: 'hsl(0, 80%, 45%)',
            data: summaryList.map(m => m.avgAirTemperatureCentigrade),
            showTooltip: true,
            tooltipValueFormatter: (value: number) => this.formatCentigrade(value)
        }, {
            type: 'line',
            label: 'min Temperatur',
            borderWidth: 0,
            backgroundColor: 'hsla(0, 80%, 45%, 0.3)',
            data: summaryList.map(m => m.minAirTemperatureCentigrade),
            showTooltip: true,
        }, {
            type: 'line',
            label: 'max Temperatur',
            borderWidth: 0,
            backgroundColor: 'hsla(0, 80%, 45%, 0.3)',
            data: summaryList.map(m => m.maxAirTemperatureCentigrade),
            showTooltip: true,
            fill: "-1",
        }]
    }

    private formatCentigrade(value: number) {
        return this.numberFormat.format(value) + " °C";
    }


    private formatMillimeters(value: number) {
        return this.numberFormat.format(value) + " mm";
    }
}

export type TemperatureRecord = BaseRecord & {
    avgAirTemperatureCentigrade: number,
    maxAirTemperatureCentigrade: number,
    minAirTemperatureCentigrade: number,
}
