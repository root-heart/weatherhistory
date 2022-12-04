import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {FilterChangedEvent, StationAndDateFilterComponent} from "../filter-header/station-and-date-filter.component";
import {environment} from "../../environments/environment";
import {ChartResolution, getDefaultChartOptions} from "./BaseChart";
import {Chart, ChartConfiguration, ChartOptions, registerables} from "chart.js";
import {HttpClient} from "@angular/common/http";

export type Sum = {
    firstDay: Date,
    sum1: number,
    sum2: number
}

@Component({
    selector: "sum-chart[filterComponent][path]",
    template: "<canvas #chart></canvas>"
})
export class SumChart {
    @Input() fill: string = "#cc3333"
    @Input() fill2: string = "#3333cc"
    @Input() path: string = "sunshine-duration"

    @Input() set filterComponent(c: StationAndDateFilterComponent) {
        c.onFilterChanged.subscribe((event: FilterChangedEvent) => {
            let stationId = event.station.id;
            let year = event.start;
            let url = `${environment.apiServer}/stations/${stationId}/${this.path}/${this.resolution}/${year}`
            this.http
                .get<Sum[]>(url)
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
            // labels: ["Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"],
            ticks: {minRotation: 0, maxRotation: 0, sampleSize: 12},
            display: this.showAxes
        }
        options.scales!.x2 = {display: false}

        const labels = data.map(d => new Date(d.firstDay!));

        let config: ChartConfiguration = {
            type: "bar",
            options: options,
            data: {
                labels: labels,
                datasets: [{
                    borderWidth: 0,
                    backgroundColor: this.fill2,
                    data: data.map(d => d.sum2),
                    categoryPercentage: 0.8,
                    barPercentage: 0.6,
                    xAxisID: "x2"
                }, {
                    backgroundColor: this.fill,
                    data: data.map(d => d.sum1),
                    categoryPercentage: 0.8,
                    barPercentage: 1,
                    xAxisID: "x"
                }]
            }
        }

        this.chart = new Chart(context, config)
    }
}