import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord} from "../BaseChart";
import {ChartDataset} from "chart.js";

export type HumidityRecord = BaseRecord & {
    minHumidityPercent: number,
    avgHumidityPercent: number,
    maxHumidityPercent: number,
}

@Component({
    selector: 'humidity-chart',
    template: '<canvas #chart></canvas>',
    styleUrls: ['../charts.css']
})
export class HumidityChart extends BaseChart<HumidityRecord> implements OnInit {
    @ViewChild("chart")
    private canvas?: ElementRef;

    constructor() {
        super();
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(data: Array<HumidityRecord>): Array<ChartDataset> {
        return [{
            type: "line",
            label: 'Temperatur',
            borderColor: 'hsl(220, 80%, 30%)',
            backgroundColor: 'hsl(220, 80%, 30%)',
            data: data.map(m => m.avgHumidityPercent),
            stack: "zero",
        }, {
            type: 'bar',
            label: 'Temperatur min/max',
            borderColor: 'hsl(220, 80%, 30%)',
            backgroundColor: 'hsla(220, 80%, 30%, 0.3)',
            data: data.map(m => [m.minHumidityPercent, m.maxHumidityPercent]),
            stack: "one",
        }]
    }

}
