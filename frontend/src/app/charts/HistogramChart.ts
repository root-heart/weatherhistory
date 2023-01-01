import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {ChartResolution, getDefaultChartOptions} from "./BaseChart";
import {Chart, ChartConfiguration, ChartDataset, ChartOptions, registerables} from "chart.js";
import ChartjsPluginStacked100 from "chartjs-plugin-stacked100";
import {Observable} from "rxjs";
import {SummaryData} from "../SummaryData";

export type Histogram = {
    firstDay: Date,
    histogram: number[]
}

@Component({
    selector: "histogram-chart[filterComponent]",
    template: "<canvas #chart></canvas>"
})
export class HistogramChart {
    @Input() fill: string = "#cc3333"
    @Input() fill2: string = "#3333cc"
    @Input() path: string = "cloud-coverage"

    @Input() set filterComponent(c: Observable<SummaryData | undefined>) {
        c.subscribe(event => {
            let data = event?.details?.map(m => {
                return <Histogram>{firstDay: m.firstDay, histogram: m.cloudCoverage}
            })
            this.setData(data || [])
        })
    }

    @Input() includeZero: boolean = true
    @Input() showAxes: boolean = true
    @Input() resolution: ChartResolution = "monthly"

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

    public setData(data: Array<Histogram>): void {
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
        options.scales!.x = {
            type: "time",
            time: {
                unit: "month",
                displayFormats: {
                    month: "MMM",
                    round: "day",
                    bound: "ticks"
                }
            },
            ticks: {minRotation: 0, maxRotation: 0, sampleSize: 12},
            display: this.showAxes
        }
        options.scales!.x2 = {display: false}

        if (this.resolution == "daily") {
            options.scales!.x!.time = {
                unit: "day",
                displayFormats: {day: "dd.MM."}
            }
        }

        const labels = data.map(d => d.firstDay);

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
