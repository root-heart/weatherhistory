import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord, MeasurementDataSet} from "../BaseChart";

export type WindSpeedRecord = BaseRecord & {
    avgWindSpeedMetersPerSecond: number,
    maxWindSpeedMetersPerSecond: number
}

@Component({
    selector: 'wind-speed-chart',
    template: '<canvas #windSpeedChart></canvas>',
    styleUrls: ['../charts.css']
})
export class WindSpeedChart extends BaseChart<WindSpeedRecord> implements OnInit {
    @ViewChild("windSpeedChart")
    private canvas?: ElementRef;

    constructor() {
        super();
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: Array<WindSpeedRecord>): Array<MeasurementDataSet> {
        return [{
            type: 'line',
            label: 'Windgeschwindigkeit',
            borderColor: 'hsl(270, 80%, 65%)',
            borderWidth: 2,
            backgroundColor: 'hsl(270, 80%, 65%)',
            data: summaryList.map(m => m.avgWindSpeedMetersPerSecond),
            pointRadius: 0,
            pointHitRadius: 20,

            showTooltip: true,
            showLegend: true,
            // tooltipValueFormatter: (value: number) => this.formatCentigrade(value)
        }, {
            type: 'bar',
            label: 'Windgeschwindigkeit',
            borderColor: 'hsla(270, 80%, 65%, 0.3)',
            borderWidth: 0,
            backgroundColor: 'hsla(270, 80%, 65%, 0.3)',
            data: summaryList.map(m => m.maxWindSpeedMetersPerSecond),
            showTooltip: true,
            showLegend: true,
            // tooltipValueFormatter: (value: number) => this.formatCentigrade(value)
        }]
    }

}
