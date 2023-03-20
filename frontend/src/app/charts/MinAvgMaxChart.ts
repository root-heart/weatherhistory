import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {Chart, ChartConfiguration, ChartOptions, registerables} from "chart.js";
import {ChartResolution, getDefaultChartOptions, getXScale} from "./BaseChart";
import 'chartjs-adapter-luxon';
import {
    AvgMaxDetails,
    DailyMeasurement,
    MinAvgMaxDetails,
    MonthlySummary,
    SummarizedMeasurement,
    SummaryData, YearlySummary
} from "../data-classes";
import {Observable} from "rxjs";
import {DateTime} from "luxon";

export type MinAvgMaxSummary = {
    firstDay: string,
    min: number,
    avg: number,
    max: number,
}


type MinAvgMaxDetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends MinAvgMaxDetails ? K : never
}[keyof SummarizedMeasurement]

type AvgMaxDetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends AvgMaxDetails ? K : never
}[keyof SummarizedMeasurement]

/**
 * If either filterComponent or path are not defined, this selector will not match. This ensures that at least these
 * properties are set
 */
@Component({
    selector: "min-avg-max-chart[]",
    template: "<canvas #chart></canvas>"
})
export class MinAvgMaxChart {
    @Input() color: string = "#c33"
    @Input() fill: string = "#cc333320"
    @Input() lineWidth: number = 2

    @Input() property?: MinAvgMaxDetailsProperty
    @Input() logarithmic: boolean = false
    @Input() minValue?: number
    @Input() maxValue?: number
    @Input() ticks?: Array<{ value: number, label: string }>

    @Input() set dataSource(c: Observable<SummaryData | undefined>) {
        c.subscribe(summaryData => {
            if (summaryData) {
                // TODO this is duplicated in the other chart classes
                let dateFunction: (m: DailyMeasurement | MonthlySummary | YearlySummary) => string
                if (summaryData.details[0] instanceof DailyMeasurement) {
                    dateFunction = (m: DailyMeasurement) => m.date!
                } else if (summaryData.details[0] instanceof MonthlySummary) {
                    dateFunction = (m: MonthlySummary) => m.year + "-" + m.month
                } else {
                    dateFunction = (m: YearlySummary) => "" + m.year
                }
                let minAvgMaxData: MinAvgMaxSummary[] = summaryData.details
                    .map(m => {
                        if (m && this.property) {
                            return {
                                firstDay: dateFunction(m),
                                min: m.measurements![this.property!].min,
                                avg: m.measurements![this.property!].avg,
                                max: m.measurements![this.property!].max
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

    @ViewChild("chart") private canvas?: ElementRef
    private chart?: Chart

    constructor() {
        Chart.register(...registerables);
    }

    public setData(data: Array<MinAvgMaxSummary>, resolution: ChartResolution): void {
        if (this.chart) {
            this.chart.destroy();
        }

        if (!this.canvas) {
            return;
        }
        let context = <CanvasRenderingContext2D>this.canvas.nativeElement.getContext('2d');

        let options: ChartOptions = getDefaultChartOptions()

        if (this.logarithmic) {
            options.scales!.y! = {
                type: "logarithmic",
                display: this.showAxes,
            }
            if (this.minValue) {
                options.scales!.y!.min = this.minValue
            }
            if (this.maxValue) {
                options.scales!.y!.max = this.maxValue
            }
            if (this.ticks) {
                options.scales!.y!.min = this.ticks[0].value
                options.scales!.y!.max = this.ticks[this.ticks.length - 1].value
                options.scales!.y!.afterBuildTicks = (chart) => {
                    chart.ticks = this.ticks!
                }
            }
        } else {
            options.scales!.y = {
                beginAtZero: this.includeZero,
                display: this.showAxes,
            }
        }

        getXScale(data, resolution, options, this.showAxes)

        const labels = data.map(d => d.firstDay);

        let config: ChartConfiguration = {
            type: "line",
            options: options,
            data: {
                labels: labels,
                datasets: [{
                    borderWidth: this.lineWidth,
                    borderColor: this.color,
                    backgroundColor: this.color,
                    data: data.map(d => d.avg)
                }, {
                    borderWidth: 0,
                    backgroundColor: this.fill,
                    data: data.map(d => d.min)
                }, {
                    borderWidth: 0,
                    backgroundColor: this.fill,
                    data: data.map(d => d.max),
                    fill: "-1",
                }]
            }
        }

        this.chart = new Chart(context, config)
    }
}
