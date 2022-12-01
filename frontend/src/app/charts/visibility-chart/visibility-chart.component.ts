import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord} from "../BaseChart";
import {ChartDataset} from "chart.js";

export type VisibilityRecord = BaseRecord & {
    minVisibilityMeters: number,
    avgVisibilityMeters: number,
    maxVisibilityMeters: number,
}

@Component({
    selector: 'visibility-chart',
    template: '<canvas #chart></canvas>',
    styleUrls: ['../charts.css']
})
export class VisibilityChart extends BaseChart<VisibilityRecord> implements OnInit {
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

    protected getDataSets(data: Array<VisibilityRecord>): Array<ChartDataset> {
        return [{
            type: "line",
            label: 'Temperatur',
            borderColor: 'hsl(0, 0%, 70%)',
            backgroundColor: 'hsl(0, 0%, 70%)',
            data: data.map(m => m.avgVisibilityMeters),
            stack: "zero",
        }, {
            type: 'bar',
            label: 'Temperatur min/max',
            borderColor: 'hsl(0, 0%, 70%)',
            backgroundColor: 'hsla(0, 0%, 70%, 0.3)',
            data: data.map(m => [m.minVisibilityMeters, m.maxVisibilityMeters]),
            stack: "one",
        }]
    }

}
