import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {FilterChangedEvent, StationAndDateFilterComponent} from "../filter-header/station-and-date-filter.component";
import {environment} from "../../environments/environment";
import {ChartResolution, getDefaultChartOptions} from "./BaseChart";
import {Chart, ChartConfiguration, ChartDataset, ChartOptions, registerables, ScriptableContext} from "chart.js";
import {HttpClient} from "@angular/common/http";

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
    @Input() path: string = "cloudiness"

    @Input() set filterComponent(c: StationAndDateFilterComponent) {
        c.onFilterChanged.subscribe((event: FilterChangedEvent) => {
            let stationId = event.station.id;
            let year = event.start;
            let url = `${environment.apiServer}/stations/${stationId}/${this.path}/${this.resolution}/${year}`
            this.http
                .get<Histogram[]>(url)
                .subscribe(data => this.setData(data))
        })
    }

    @Input() includeZero: boolean = true
    @Input() showAxes: boolean = true
    @Input() resolution: ChartResolution = "monthly"

    @ViewChild("chart") private canvas?: ElementRef
    private chart?: Chart

    // TODO @Input()
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

        'hsl(0, 50%, 30%)',
        'hsl(0, 50%, 20%)',
    ];

    constructor(private http: HttpClient) {
        Chart.register(...registerables);
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

        const getColor = (context: ScriptableContext<'bar'>) => {
            let bucket = context.datasetIndex
            let value = data[context.dataIndex]
            let coverage = value.histogram[bucket]
            return coverage === undefined || coverage === null ? 'hsl(0, 50%, 30%)' : this.coverageColors[coverage];
        }

        const lengths = data.map(d => d.histogram).map(h => h.length);
        let maxLength = Math.max.apply(null, lengths)
        let datasets: ChartDataset[] =  []

        for (let index = 0; index < maxLength; index++) {
            datasets[index] = {
                type: 'bar',
                label: 'Bewölkung ' + index,
                backgroundColor: this.coverageColors[index],
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