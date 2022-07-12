import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, MeasurementDataSet} from "../BaseChart";
import {
    BarController,
    BarElement,
    CategoryScale,
    Chart, Filler, Legend,
    LinearScale,
    LineController,
    LineElement,
    PointElement, Tooltip, TimeScale
} from "chart.js";
import {SummaryJson, SummaryList} from "../SummaryService";
import 'chartjs-adapter-moment';

@Component({
    selector: 'sunshine-chart',
    template: '<h1>Sonnenschein</h1><div style="height: 30vw"><canvas #sunshineChart></canvas></div>',
})
export class SunshineChart extends BaseChart implements OnInit {
    @ViewChild("sunshineChart")
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

    protected getDataSets(summaryList: SummaryList): Array<MeasurementDataSet> {
        let dataSets: Array<MeasurementDataSet> = [];
        dataSets.push({
            label: 'Sonnenschein',
            borderColor: 'hsl(40, 100%, 50%)',
            backgroundColor: 'hsl(40, 100%, 50%)',
            data: summaryList.map(m => m['sumSunshineDurationHours']),
            stack: 'sunshine',
            categoryPercentage: 0.7,
            barPercentage: 1,
            showTooltip: true,
            showLegend: false,
            tooltipValueFormatter: (value: number) => this.formatHours(value)
        });
        return dataSets;
    }

    protected getScales(): any {
        return {
        }
    }

    private formatHours(value: number) {
        return this.numberFormat.format(value) + " h";
    }

    private formatPercent(value: number) {
        return this.numberFormat.format(value) + " %";
    }
}
