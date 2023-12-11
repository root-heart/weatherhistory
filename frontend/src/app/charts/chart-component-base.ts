import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {FilterService} from "../filter.service";
import {SummaryData} from "../data-classes";

addMore(Highcharts);

export abstract class ChartComponentBase {
    Highcharts: typeof Highcharts = Highcharts;
    chart?: Highcharts.Chart;
    chartOptions: Highcharts.Options = {
        chart: {styledMode: true, animation: false, zooming: {mouseWheel: {enabled: true}, type: "x"}},
        legend: {enabled: false},
        title: {text: undefined},
        tooltip: {
            shared: true,
            xDateFormat: "%d.%m.%Y",
            animation: false
        },
        plotOptions: {
            line: {animation: false},
            arearange: {animation: false},
            column: {animation: false}

        },
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

    protected constructor(filterService: FilterService) {
        filterService.currentData.subscribe(summaryData => {
            if (summaryData) {
                this.setChartData(summaryData)
                    .then(() => setTimeout(() => this.chart?.redraw(), 0))
            }
        })
    }

    chartCallback: Highcharts.ChartCallbackFunction = c => {
        this.chart = c
        // this.getYAxes().forEach(a => c.addAxis(a))
        let colorAxis = this.getColorAxis();
        if (colorAxis) {
            c.addColorAxis(colorAxis)
        }
        this.createSeries(c)
    }

    protected abstract setChartData(summaryData: SummaryData): Promise<void>

    protected abstract createSeries(chart: Highcharts.Chart): void

    protected getYAxes(): Highcharts.AxisOptions[] {
        return [{
            id: "yAxis",
            title: {text: undefined},
            reversedStacks: false
        }]
    }

    protected getColorAxis(): Highcharts.ColorAxisOptions | null {
        return null
    }
}
