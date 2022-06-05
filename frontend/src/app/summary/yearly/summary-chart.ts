import {Component, ElementRef, Injectable, ViewChild} from '@angular/core';
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
    Tooltip
} from 'chart.js';
import {SummaryList} from "../SummaryService";
import {BaseChart, MeasurementDataSet} from "../BaseChart";


@Component({
    selector: 'yearly-summary-chart',
    template: '<div style="height:90vw"><canvas #yearlySummaryChart></canvas></div>'
})
@Injectable({providedIn: 'root'})
export class SummaryChart extends BaseChart {
    @ViewChild("yearlySummaryChart")
    private canvas?: ElementRef;


    constructor() {
        super();
        Chart.register(BarController, BarElement, LineController, LineElement, PointElement, CategoryScale, LinearScale, Tooltip, Legend, Filler);
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: SummaryList): Array<MeasurementDataSet> {
        return [];
    }

    protected getYScales(): any {
        return {};
    }

    /*
        function addDewPointTemperatureDataSets(dataSets, summaryList) {
          dataSets.push({
            type: 'line',
            label: 'Taupunkt Durchschnitt',
            borderColor: 'hsl(300, 50%, 45%)',
            backgroundColor: 'hsl(300, 50%, 45%)',
            data: summaryList.map(m => m['avgDewPointTemperatureCentigrade']),
            xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature'
          });

          dataSets.push({
            type: 'line',
            label: 'Taupunkt Minimum',
            borderColor: 'hsla(300, 50%, 45%, 0.3)',
            backgroundColor: 'hsla(300, 50%, 45%, 0.3)',
            data: summaryList.map(m => m['minDewPointTemperatureCentigrade']),
            xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature'
          });

          dataSets.push({
            type: 'line',
            label: 'Taupunkt Maximum',
            borderColor: 'hsla(300, 50%, 45%, 0.3)',
            backgroundColor: 'hsla(300, 50%, 45%, 0.3)',
            data: summaryList.map(m => m['maxDewPointTemperatureCentigrade']),
            xAxisID: 'xAxisVisible',
            yAxisID: 'yAxisTemperature',
            fill: '-1'
          });
        }
        */

}

