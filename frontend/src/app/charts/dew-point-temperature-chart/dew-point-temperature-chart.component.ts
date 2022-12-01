import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord} from "../BaseChart";
import {ChartDataset} from "chart.js";

export type DewPointTemperatureRecord = BaseRecord & {
    minDewPointTemperatureCentigrade: number,
    avgDewPointTemperatureCentigrade: number,
    maxDewPointTemperatureCentigrade: number,
}

@Component({
    selector: 'dew-point-temperature-chart',
    template: '<canvas #dewPointTemperatureChart></canvas>',
    styleUrls: ['../charts.css']
})
export class DewPointTemperatureChart extends BaseChart<DewPointTemperatureRecord> implements OnInit {
    @ViewChild("dewPointTemperatureChart")
    private canvas?: ElementRef;

    constructor() {
        super();
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(data: Array<DewPointTemperatureRecord>): Array<ChartDataset> {
        return [{
            type: "line",
            label: 'Temperatur',
            borderColor: 'hsl(220, 80%, 70%)',
            backgroundColor: 'hsl(220, 80%, 70%)',
            data: data.map(m => m.avgDewPointTemperatureCentigrade),
            stack: "zero",
        }, {
            type: 'bar',
            label: 'Temperatur min/max',
            borderColor: 'hsl(220, 80%, 70%)',
            backgroundColor: 'hsla(220, 80%, 70%, 0.3)',
            data: data.map(m => [m.minDewPointTemperatureCentigrade, m.maxDewPointTemperatureCentigrade]),
            stack: "one",
        }]
    }

}
