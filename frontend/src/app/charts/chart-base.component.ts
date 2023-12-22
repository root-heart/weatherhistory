import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {Component, Input} from "@angular/core";
import {WeatherStation} from "../WeatherStationService";
import {HttpClient} from "@angular/common/http";
import {environment} from "../environments/environment";
import {firstValueFrom} from "rxjs";

addMore(Highcharts);

// TODO there is some duplication in extending classes, perhaps this can be removed by composition?
@Component({
    template: ""
})
export abstract class ChartBaseComponent<T> {
    Highcharts: typeof Highcharts = Highcharts;
    chart?: Highcharts.Chart;
    chartOptions: Highcharts.Options = {
        chart: {
            styledMode: true,
            animation: false
        },
        legend: {
            enabled: false
        },
        title: {text: undefined},
        tooltip: {
            formatter: this.getTooltipText,
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false},
            column: {animation: false}

        },
        series: this.createSeries(),
        xAxis: {
            id: "xAxis",
            crosshair: true,
            type: 'datetime',
            labels: {
                formatter: v => new Date(v.value).toLocaleDateString('de-DE', {day: "numeric", month: "short"})
            },
            ordinal: true
        },
        yAxis: this.getYAxes()
    }

    constructor(private http: HttpClient) {
    }

    @Input() set name(name: string) {
        let series = this.chartOptions.series as Highcharts.SeriesOptionsType[]
        series.forEach(s => {
            s.name = name
        })
    }

    @Input() set unit(unit: string) {
        let series = this.chartOptions.series as Highcharts.SeriesOptionsType[]
        series.forEach(s => {
            // @ts-ignore
            s.tooltip = {valueSuffix: unit}
        })
    }

    @Input() set yAxisLabelFormatter(f: Highcharts.AxisLabelsFormatterCallbackFunction) {
        let yAxes = this.chartOptions.yAxis as Highcharts.AxisOptions[]
        yAxes.forEach(a => {
            a.labels = {
                formatter: f
            }
        })
    }

    @Input() measurementName!: string

    chartCallback: Highcharts.ChartCallbackFunction = c => {
        this.chart = c
        let colorAxis = this.getColorAxis();
        if (colorAxis) {
            c.addColorAxis(colorAxis)
        }
    }

    update(weatherStation: WeatherStation, year: number) {
        console.log(`fetching measurement ${this.measurementName} for ${weatherStation.name} and year ${year}`)
        this.fetchData(weatherStation, year)
            .then(data => {
                this.chart?.showLoading("Aktualisiere Diagramm...");
                return data
            })
            .then(data => {
                this.setChartData(data)
            })
            .then(() => setTimeout(() => {
                this.chart?.hideLoading()
                this.chart?.redraw()
            }, 0))
    }

    protected abstract setChartData(data: T[]): Promise<void>

    protected abstract createSeries(): Highcharts.SeriesOptionsType[]

    protected getYAxes(): Highcharts.AxisOptions[] {
        return [{
            id: "yAxis",
            title: {text: undefined},
            reversedStacks: false
        }]
    }

    protected getColorAxis(): Highcharts.ColorAxisOptions | undefined {
        return undefined
    }

    protected abstract getTooltipText(_: Highcharts.Tooltip): string

    private fetchData(weatherStation: WeatherStation, year: number): Promise<T[]> {
        let url = `${environment.apiServer}/stations/${weatherStation.id}/${this.measurementName}/${year}`
        console.log(`fetching data from ${url}`)
        return firstValueFrom(this.http.get<T[]>(url))
    }
}

type TooltipPointInformation = {
    x: number,
    y: number,
    high: number,
    value: number,
    series: {
        name: string,
        tooltipOptions: {
            valueSuffix: string
        }
    },
    custom: {
        tooltipFormatter: (originalValue: number) => string
    }
}

export type TooltipInformation = {
    color: string,
    colorIndex: number,
    key: any,
    percentage: any,
    point: TooltipPointInformation,
    points: {
        point: TooltipPointInformation
    }[],
    total: number,
    x: number,
    y: number
}
