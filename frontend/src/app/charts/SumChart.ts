import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {ChartResolution, getDefaultChartOptions, getXScale} from "./BaseChart";
import {Chart, ChartConfiguration, ChartOptions, registerables} from "chart.js";
import {HttpClient} from "@angular/common/http";
import {Measurement, MinAvgMaxDetails, MinMaxSumDetails, SummaryData} from "../SummaryData";
import {Observable} from "rxjs";

export type Sum = {
    firstDay: string,
    sum: number
}

type MinMaxSumDetailsProperty = {
    [K in keyof Measurement]: Measurement[K] extends MinMaxSumDetails ? K : never
}[keyof Measurement]


@Component({
    selector: "sum-chart",
    template: "<canvas #chart></canvas>"
})
export class SumChart {
    @Input() fill: string = "#cc3333"
    @Input() fill2: string = "#3333cc"
    @Input() sum?: MinMaxSumDetailsProperty
    @Input() valueConverter: (x?: number) => number | undefined = x => x

    @Input() set dataSource(c: Observable<SummaryData | undefined>) {
        c.subscribe(summaryData => {
            if (summaryData) {
                let minAvgMaxData = summaryData.details
                    .map(m => {
                        if (this.sum) {
                            let s = m[this.sum].sum
                            return <Sum>{
                                firstDay: m.firstDay,
                                sum: s == null ? null : this.valueConverter(s)
                            }
                        } else {
                            return null
                        }
                    })
                    .filter(d => d !== null && d !== undefined)
                    .map(d => d!)
                this.setData(minAvgMaxData, summaryData.resolution)
            } else {
                this.setData([], "month")
            }
        })
    }

    @Input() includeZero: boolean = true
    @Input() showAxes: boolean = true
    // @Input() resolution: ChartResolution = "monthly"

    @ViewChild("chart") private canvas?: ElementRef
    private chart?: Chart

    constructor(private http: HttpClient) {
        Chart.register(...registerables);
    }

    public setData(data: Array<Sum>, resolution: ChartResolution): void {
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
        options.scales!.x2 = {display: false}

        getXScale(data, resolution, options, this.showAxes)

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
