import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, MeasurementDataSet} from "../BaseChart";
import {SummaryJson, SummaryList} from "../SummaryService";
import {
    CategoryScale,
    Chart,
    Filler,
    Legend,
    LinearScale,
    LineController,
    LineElement,
    PointElement,
    Tooltip
} from "chart.js";

@Component({
    selector: 'cloudiness-chart',
    template: '<h1>Bewölkung</h1><div style="height: 30vw"><canvas #cloudinessChart></canvas></div>',
})
export class CloudinessChart extends BaseChart implements OnInit {
    @ViewChild("cloudinessChart")
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
        Chart.register( LineController, LineElement, PointElement, CategoryScale, LinearScale, Tooltip, Legend, Filler);
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: SummaryList): Array<MeasurementDataSet> {
        let dataSets: Array<MeasurementDataSet> = [];
        // let sums = summaryList.map(m => this.calculateSumOfCoverageMeasures(m));

        for (let i = 0; i <= 8; i++) {
            dataSets.push({
                label: this.coverageNames[i],
                borderColor: this.coverageColors[i],
                // borderWidth: 0,
                backgroundColor: this.coverageColors[i],
                data: summaryList.map(m => m[("countCloudCoverage" + i) as keyof SummaryJson] as number / this.calculateSumOfCoverageMeasures(m)) as number[],
                xAxisID: 'x1',
                categoryPercentage: 0.7,
                stack: 'cloudiness',
                showTooltip: true,
                showLegend: false,
                tooltipValueFormatter: (value: number) => this.formatHours(value)
            });
        }

        dataSets.push({
            label: this.coverageNames[9],
            borderColor: this.coverageColors[9],
            backgroundColor: this.coverageColors[9],
            data: summaryList.map(m => m.countCloudCoverageNotVisible / this.calculateSumOfCoverageMeasures(m)),
            stack: 'cloudiness',
            xAxisID: 'x1',
            categoryPercentage: 0.7,
            // barPercentage: 1,
            showTooltip: true,
            showLegend: true,
            tooltipValueFormatter: (value: number) => this.formatHours(value)
        });
        return dataSets;
    }

    private calculateSumOfCoverageMeasures(m: SummaryJson) : number {
        return m.countCloudCoverage0
        + m.countCloudCoverage1
        + m.countCloudCoverage2
        + m.countCloudCoverage3
        + m.countCloudCoverage4
        + m.countCloudCoverage5
        + m.countCloudCoverage6
        + m.countCloudCoverage7
        + m.countCloudCoverage8
        + m.countCloudCoverageNotMeasured
        + m.countCloudCoverageNotVisible
    }

    protected getYScales(): any {
        return {
            x1: {
                stacked: true,
            },
            yAxisHours: {
                display: true,
                stacked: true
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
