import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {Chart, ChartConfiguration, ChartOptions, registerables} from "chart.js";
import {ChartResolution, getDefaultChartOptions} from "./BaseChart";
import {StationAndDateFilterComponent} from "../filter-header/station-and-date-filter.component";
import {HttpClient} from "@angular/common/http";
import 'chartjs-adapter-luxon';
import {Measurement} from "../SummaryData";

export type MinAvgMaxSummary = {
    firstDay: Date,
    min: number,
    avg: number,
    max: number,
}

/**
 * If either filterComponent or path are not defined, this selector will not match. This ensures that at least these
 * properties are set
 */
@Component({
    selector: "min-avg-max-chart[filterComponent]",
    template: "<canvas #chart></canvas>"
})
export class MinAvgMaxChart {
    @Input() color: string = "#c33"
    @Input() fill: string = "#cc333320"
    @Input() lineWidth: number = 2

    // @Input() path: string = "temperature"
    @Input() min?: keyof Measurement
    @Input() avg?: keyof Measurement
    @Input() max?: keyof Measurement
    @Input() logarithmic: boolean = false
    @Input() minValue?: number
    @Input() maxValue?: number

    @Input() set filterComponent(c: StationAndDateFilterComponent) {
        c.onFilterChanged.subscribe(event => {
            let minAvgMaxData = event.details.map(m => {
                return <MinAvgMaxSummary>{
                    firstDay: m.firstDay,
                    min: this.min ? m[this.min] : 0,
                    avg: this.avg ? m[this.avg] : 0,
                    max: this.max ? m[this.max] : 0
                }
            })
            this.setData(minAvgMaxData)
        })
    }

    @Input() includeZero: boolean = true
    @Input() showAxes: boolean = true
    @Input() resolution: ChartResolution = "monthly"

    @ViewChild("chart") private canvas?: ElementRef
    private chart?: Chart

    constructor() {
        Chart.register(...registerables);
    }

    public setData(data: Array<MinAvgMaxSummary>): void {
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
        } else {
            options.scales!.y = {
                beginAtZero: this.includeZero,
                display: this.showAxes,
            }
        }

        options.scales!.x = {
            type: "time",
            time: {
                unit: "month",
                displayFormats: {
                    month: "MMM"
                }
            },
            // labels: ["Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"],
            ticks: {minRotation: 0, maxRotation: 0, sampleSize: 12},
            display: this.showAxes
        }

        const labels = data.map(d => d.firstDay);

        let config: ChartConfiguration = {
            type: "line",
            options: options,
            data: {
                labels: labels,
                datasets: [{
                    // type: "line",
                    // label: 'Temperatur',
                    borderWidth: this.lineWidth,
                    borderColor: this.color,
                    backgroundColor: this.color,
                    data: data.map(d => d.avg)
                }, {
                    // type: 'line',
                    // label: 'min Temperatur',
                    borderWidth: 0,
                    backgroundColor: this.fill,
                    data: data.map(d => d.min)
                }, {
                    // type: 'line',
                    // label: 'max Temperatur',
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
