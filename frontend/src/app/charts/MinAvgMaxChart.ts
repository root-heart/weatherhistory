import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {Chart, ChartConfiguration, ChartOptions, TimeScaleOptions} from "chart.js";
import {ChartResolution, getDefaultChartOptions} from "./BaseChart";
import {FilterChangedEvent, StationAndDateFilterComponent} from "../filter-header/station-and-date-filter.component";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {registerables} from 'chart.js';
import 'chartjs-adapter-luxon';

export type MinAvgMaxSummary = {
    firstDay?: Date,
    min: number,
    avg: number,
    max: number,
}

/**
 * If either filterComponent or path are not defined, this selector will not match. This ensures that at least these
 * properties are set
 */
@Component({
    selector: "min-avg-max-chart[filterComponent][path]",
    template: "<canvas #chart></canvas>"
})
export class MinAvgMaxChart {
    @Input() color: string = "#c33"
    @Input() fill: string = "#cc333320"
    @Input() lineWidth: number = 1

    @Input() path: string = "temperature"

    @Input() set filterComponent(c: StationAndDateFilterComponent) {
        c.onFilterChanged.subscribe((event: FilterChangedEvent) => {
            let stationId = event.station.id;
            let year = event.start;
            let url = `${environment.apiServer}/stations/${stationId}/${this.path}/${this.resolution}/${year}`
            this.http
                .get<MinAvgMaxSummary[]>(url)
                .subscribe(data => this.setData(data))
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

    public setData(data: Array<MinAvgMaxSummary>): void {
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
                    month: "MMMMM"
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