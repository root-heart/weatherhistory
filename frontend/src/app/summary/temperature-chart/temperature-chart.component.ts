import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, MeasurementDataSet} from "../BaseChart";
import {SummaryList} from "../SummaryService";
import {
    BarController,
    BarElement, CartesianScaleOptions,
    CategoryScale,
    Chart,
    Filler,
    Legend,
    LinearScale, LinearScaleOptions,
    LineController,
    LineElement,
    PointElement,
    Tooltip
} from "chart.js";

@Component({
    selector: 'temperature-chart',
    template: '<h1>Temperatur</h1><div style="height: 30vw"><canvas #temperatureChart></canvas></div>'
})
export class TemperatureChart extends BaseChart implements OnInit {
    @ViewChild("temperatureChart")
    private canvas?: ElementRef;

    constructor() {
        super();
        Chart.register(BarController, BarElement, LineController, LineElement, PointElement, CategoryScale, LinearScale, Tooltip, Legend, Filler);
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
            label: 'Temperatur',
            borderColor: 'hsl(0, 80%, 45%)',
            backgroundColor: 'hsl(0, 80%, 45%)',
            data: summaryList.map(m => m['avgAirTemperatureCentigrade']),
            // xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature',
            pointRadius: 0,
            pointHitRadius: 20,
            showTooltip: true,
            showLegend: true,
            tooltipValueFormatter: (value: number) => this.formatCentigrade(value)
        });

        dataSets.push({
            type: 'line',
            label: 'Minimum Temperatur',
            borderColor: 'hsla(0, 80%, 45%, 0.3)',
            backgroundColor: 'hsla(0, 80%, 45%, 0.15)',
            data: summaryList.map(m => m['minAirTemperatureCentigrade']),
            // xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature',
            pointRadius: 0,
            pointHitRadius: 20,
            showTooltip: false
        });

        dataSets.push({
            type: 'line',
            label: 'Maximum Temperatur',
            borderColor: 'hsla(0, 80%, 45%, 0.3)',
            backgroundColor: 'hsla(0, 80%, 45%, 0.15)',
            data: summaryList.map(m => m['maxAirTemperatureCentigrade']),
            // xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature',
            fill: '-1',
            pointRadius: 0,
            pointHitRadius: 20,
            showTooltip: false
        });

        // dataSets.push({
        //     label: 'Regen',
        //     borderColor: 'hsl(240, 80%, 35%)',
        //     backgroundColor: 'hsl(240, 80%, 35%)',
        //     data: summaryList.map(m => m['sumRainfallMillimeters']),
        //     // xAxisID: 'xAxisVisible',
        //     yAxisID: 'yAxisMillimeters',
        //     categoryPercentage: 0.5,
        //     barPercentage: 0.8,
        //     showTooltip: true,
        //     showLegend: true,
        //     tooltipValueFormatter: (value: number) => this.formatMillimeters(value)
        // });

        return dataSets;
    }

    private formatCentigrade(value: number) {
        return this.numberFormat.format(value) + " °C";
    }


    private formatMillimeters(value: number) {
        return this.numberFormat.format(value) + " mm";
    }
}
