import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {ChartResolution, getDateLabel, getDefaultChartOptions, getXScale} from "./BaseChart";
import {Chart, ChartConfiguration, ChartDataset, ChartOptions, registerables} from "chart.js";
import ChartjsPluginStacked100 from "chartjs-plugin-stacked100";
import {Observable} from "rxjs";
import {SummaryData} from "../data-classes";

export type Histogram = {
    dateLabel: string,
    histogram: number[]
}

@Component({
    selector: "histogram-chart",
    template: "<canvas #chart></canvas>"
})
export class HistogramChart {
    @Input() fill: string = "#cc3333"
    @Input() fill2: string = "#3333cc"
    @Input() path: string = "cloud-coverage"

    @Input() set dataSource(c: Observable<SummaryData  | undefined>) {
        c.subscribe(summaryData => {
            if (summaryData) {
                let data = summaryData.details?.map(m => {
                    return <Histogram>{
                        dateLabel: getDateLabel(m),
                        histogram: m.measurements!.cloudCoverageHistogram
                    }
                })
                this.setData(data, summaryData.resolution)
            } else {
                this.setData([], "month")
            }
        })
    }

    @Input() includeZero: boolean = true
    @Input() showAxes: boolean = true
    @ViewChild("chart") private canvas?: ElementRef
    private chart?: Chart

    // TODO @Input()
    private readonly coverageColors = [
        {name: "wolkenlos", color: "hsl(210, 80%, 80%)"},
        {name: "sonnig", color: "hsl(210, 90%, 95%)"},
        {name: "heiter", color: "hsl(55, 80%, 90%)"},
        {name: "leicht bewölkt", color: "hsl(55, 65%, 80%)"},
        {name: "wolkig", color: "hsl(55, 45%, 70%)"},
        {name: "bewölkt", color: "hsl(55, 25%, 70%)"},
        {name: "stark bewölkt", color: "hsl(55, 5%, 65%)"},
        {name: "fast bedeckt", color: "hsl(55, 5%, 55%)"},
        {name: "bedeckt", color: "hsl(55, 5%, 45%)"},
        {name: "Himmel nicht erkennbar", color: "hsl(55, 5%, 35%)"},
    ];

    constructor() {
        Chart.register(...registerables, ChartjsPluginStacked100);
    }

    public setData(data: Array<Histogram>, resolution: ChartResolution): void {
        if (this.chart) {
            this.chart.destroy();
        }

        if (!this.canvas) {
            return;
        }
        let context = <CanvasRenderingContext2D>this.canvas.nativeElement.getContext('2d');

        let options: ChartOptions = getDefaultChartOptions()

        options.plugins!.stacked100 = {enable: true}
        options.scales!.y = {
            beginAtZero: this.includeZero,
            display: this.showAxes
        }

        getXScale(data, resolution, options, this.showAxes)
        options.scales!.x2 = {display: false}

        const labels = data.map(d => d.dateLabel);
        // TODO this does not work yet
        const lengths = data.map(d => d.histogram).map(h => h.length);
        let maxLength = Math.max.apply(null, lengths)
        let datasets: ChartDataset[] = []

        for (let index = 0; index < maxLength; index++) {
            datasets[index] = {
                type: 'bar',
                label: this.coverageColors[index].name,
                backgroundColor: this.coverageColors[index].color,
                data: data.map(d => d.histogram[index]),
                categoryPercentage: 0.8,
                barPercentage: 1,
                stack: 'histogram'
            }
        }

        let config: ChartConfiguration = {
            type: "bar",
            options: options,
            data: {
                labels: labels,
                datasets: datasets
            }
        }

        this.chart = new Chart(context, config)
    }
}
