import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {FilterChangedEvent, StationAndDateFilterComponent} from "../filter-header/station-and-date-filter.component";
import {environment} from "../../environments/environment";
import {ChartResolution, getDefaultChartOptions} from "./BaseChart";
import {Chart, ChartConfiguration, ChartOptions, registerables} from "chart.js";
import {HttpClient} from "@angular/common/http";
import {Measurement, SummaryData} from "../SummaryData";
import {MinAvgMaxSummary} from "./MinAvgMaxChart";
import {Duration} from 'luxon'
import {Observable} from "rxjs";

export type Sum = {
    firstDay: Date,
    sum: number
}

@Component({
    selector: "sum-chart[filterComponent]",
    template: "<canvas #chart></canvas>"
})
export class SumChart {
    @Input() fill: string = "#cc3333"
    @Input() fill2: string = "#3333cc"
    @Input() sum: keyof Measurement = "sunshineDuration"
    @Input() valueConverter: (x?: number) => number | undefined = function (x) {
        return x
    }

    @Input() set filterComponent(c: Observable<SummaryData | undefined>) {
        c.subscribe(event => {
            let minAvgMaxData = event?.details?.map(m => {
                let s = m[this.sum] as number
                return <Sum>{
                    firstDay: m.firstDay, sum: s == null ? null : this.valueConverter(s)
                }
            })
            this.setData(minAvgMaxData || [])
        })
    }

    @Input() includeZero: boolean = true
    @Input() showAxes: boolean = true
    @Input() resolution: ChartResolution = "monthly"

    @ViewChild("chart") private canvas?: ElementRef
    private chart?: Chart

    constructor(private http: HttpClient) {
        Chart.register(...registerables);
    }

    public setData(data: Array<Sum>): void {
        if (this.chart) {
            this.chart.destroy();
        }

        if (!this.canvas) {
            return;
        }
        let context = <CanvasRenderingContext2D>this.canvas.nativeElement.getContext('2d');

        let options: ChartOptions = getDefaultChartOptions()

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

        const labels = data.map(d => d.firstDay);

        let config: ChartConfiguration = {
            type: "bar",
            options: options,
            data: {
                labels: labels,
                datasets: [{
                    backgroundColor: this.fill,
                    data: data.map(d => d.sum),
                    categoryPercentage: 0.8,
                    barPercentage: 1,
                    xAxisID: "x"
                }]
            }
        }

        this.chart = new Chart(context, config)
    }
}
