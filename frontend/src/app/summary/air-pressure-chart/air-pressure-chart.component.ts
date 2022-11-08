import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, MeasurementDataSet} from "../BaseChart";
import {SummaryList} from "../SummaryService";

@Component({
    selector: 'air-pressure-chart',
    template: '<canvas #airPressureChart></canvas>',
    styles: [':host {height: 25rem; display: block;}']
})
export class AirPressureChart extends BaseChart implements OnInit {
    @ViewChild("airPressureChart")
    private canvas?: ElementRef;


    constructor() {
        super();
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: SummaryList): Array<MeasurementDataSet> {
        let dataSets: Array<MeasurementDataSet> = [];
        dataSets.push({
            type: 'line',
            label: 'Luftdruck',
            borderColor: 'hsl(120, 80%, 45%)',
            borderWidth: 2,
            backgroundColor: 'hsl(120, 80%, 45%)',
            data: summaryList.map(m => m.avgAirPressureHectopascals),
            // xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature',
            pointRadius: 0,
            pointHitRadius: 20,

            showTooltip: true,
            showLegend: true,
            // tooltipValueFormatter: (value: number) => this.formatCentigrade(value)
        });

        dataSets.push({
            type: 'line',
            label: 'Minimum Luftdruck',
            borderColor: 'hsla(120, 80%, 45%, 0)',
            backgroundColor: 'hsla(120, 80%, 45%, 0.15)',
            data: summaryList.map(m => m.minAirPressureHectopascals),
            // xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature',
            pointRadius: 0,
            pointHitRadius: 20,
            showTooltip: false
        });

        dataSets.push({
            type: 'line',
            label: 'Maximum Luftdruck',
            borderColor: 'hsla(120, 80%, 45%, 0)',
            backgroundColor: 'hsla(120, 80%, 45%, 0.15)',
            data: summaryList.map(m => m.maxAirPressureHectopascals),
            // xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature',
            fill: '-1',
            pointRadius: 0,
            pointHitRadius: 20,
            showTooltip: false
        });

        return dataSets;
    }

}
