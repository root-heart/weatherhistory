import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {AvgMaxDetails, MinAvgMaxDetails, SummarizedMeasurement, SummaryData} from "../data-classes";
import {Observable} from "rxjs";
import {getDateLabel} from "./charts";

type MinAvgMaxSummary = {
    dateLabel: string,
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
    template: "<div style='position: relative'><div style='position: absolute; top: 0; left: 0; bottom: 0; right: 0'><canvas #chart></canvas></div></div>"
})
export class MinAvgMaxChart {
    @Input() color: string = "#c33"
    @Input() fill: string = "#cc333320"
    @Input() lineWidth: number = 2

    @Input() property?: MinAvgMaxDetailsProperty | AvgMaxDetailsProperty
    @Input() logarithmic: boolean = false
    @Input() minValue?: number
    @Input() maxValue?: number
    @Input() ticks?: Array<{ value: number, label: string }>

    @Input() set dataSource(c: Observable<SummaryData | undefined>) {
        c.subscribe(summaryData => {
            if (summaryData) {
                // TODO this is duplicated in the other chart classes
                let minAvgMaxData: MinAvgMaxSummary[] = summaryData.details
                    .map(m => {
                        if (m && this.property) {
                            let measurement = m.measurements![this.property!]
                            return {
                                dateLabel: getDateLabel(m),
                                min: "min" in measurement ? measurement.min : 0,
                                avg: measurement.avg,
                                max: measurement.max
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

    public updateChart() {
        console.log("updating chart")
        this.chart?.update()
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

        const labels = data.map(d => d.dateLabel);

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
