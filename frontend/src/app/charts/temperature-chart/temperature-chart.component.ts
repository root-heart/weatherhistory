import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord, MeasurementDataSet} from "../BaseChart";
import {
    BarController,
    BarElement,
    CategoryScale,
    Chart,
    Filler,
    Legend,
    LinearScale,
    LineController,
    LineElement,
    PointElement,
    TimeScale,
    Tooltip
} from "chart.js";

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
        Chart.register(TimeScale, BarController, BarElement, LineController, LineElement, PointElement, CategoryScale, LinearScale, Tooltip, Legend, Filler);
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
            borderColor: 'hsl(0, 80%, 45%)',
            backgroundColor: 'hsl(0, 80%, 45%)',
            data: summaryList.map(m => m.avgAirTemperatureCentigrade),
            stack: "zero",
            showTooltip: true,
            tooltipValueFormatter: (value: number) => this.formatCentigrade(value)
        }, {
            type: 'bar',
            label: 'Temperatur min/max',
            borderColor: 'hsla(0, 80%, 45%, 1)',
            backgroundColor: 'hsla(0, 80%, 45%, 0.15)',
            data: summaryList.map(m => [m.minAirTemperatureCentigrade, m.maxAirTemperatureCentigrade]),
            stack: "one",
            showTooltip: true
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
