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
    PointElement, Tooltip
} from "chart.js";
import {SummaryJson, SummaryList} from "../SummaryService";

@Component({
    selector: 'sunshine-chart',
    template: '<div style="height: 30vw"><canvas #sunshineChart></canvas></div>',
})
export class SunshineChart extends BaseChart implements OnInit {
    @ViewChild("sunshineChart")
    private canvas?: ElementRef;

    private readonly coverageNames: Array<string> = [
        'wolkenlos',
        'vereinzelte Wolken',
        'heiter',
        'leicht bewölkt',
        'wolkig',
        'bewölkt',
        'stark bewölkt',
        'fast bedeckt',
        'bedeckt',
        'nicht erkennbar',
    ];

    private readonly coverageColors = [
        'hsl(210, 80%, 80%)',
        'hsl(210, 90%, 95%)',
        'hsl(55, 80%, 90%)',
        'hsl(55, 65%, 80%)',
        'hsl(55, 45%, 70%)',
        'hsl(55, 25%, 70%)',
        'hsl(55, 5%, 65%)',
        'hsl(55, 5%, 55%)',
        'hsl(55, 5%, 45%)',
        'hsl(55, 5%, 35%)',
    ];

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
            label: 'Sonnenschein',
            borderColor: 'hsl(40, 100%, 50%)',
            backgroundColor: 'hsl(40, 100%, 50%)',
            data: summaryList.map(m => m['sumSunshineDurationHours']),
            yAxisID: 'yAxisHours',
            xAxisID: 'x1',
            stack: 'sunshine',
            categoryPercentage: 0.3,
            barPercentage: 1,
            showTooltip: true,
            showLegend: true,
            tooltipValueFormatter: (value: number) => this.formatHours(value)
        });

        for (let i = 0; i <= 8; i++) {
            dataSets.push({
                label: this.coverageNames[i],
                borderColor: this.coverageColors[i],
                backgroundColor: this.coverageColors[i],
                data: summaryList.map(m => m[("countCloudCoverage" + i) as keyof SummaryJson]) as number[],
                xAxisID: 'x2',
                categoryPercentage: 0.8,
                barPercentage: 1,
                stack: 'cloudiness',
                showTooltip: true,
                showLegend: true,
                tooltipValueFormatter: (value: number) => this.formatHours(value)
            });
        }

        dataSets.push({
            label: this.coverageNames[9],
            borderColor: this.coverageColors[9],
            backgroundColor: this.coverageColors[9],
            data: summaryList.map(m => m["countCloudCoverageNotVisible"]),
            stack: 'cloudiness',
            xAxisID: 'x2',
            categoryPercentage: 0.8,
            barPercentage: 1,
            showTooltip: true,
            showLegend: true,
            tooltipValueFormatter: (value: number) => this.formatHours(value)
        });

        return dataSets;
    }

    protected getYScales() {
        return {
            x1: {
                stacked: true,
            },
            x2: {
                stacked: true,
                display: false,
                offset: true,
            },
            yAxisHours: {
                display: true,
            }
        };
    }

    private formatHours(value: number) {
        return this.numberFormat.format(value) + " h";
    }


    private formatPercent(value: number) {
        return this.numberFormat.format(value) + " %";
    }
}
