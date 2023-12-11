import {Component, Input} from '@angular/core';
import {ChartComponentBase} from "../chart-component-base";
import * as Highcharts from "highcharts";
import {FilterService} from "../../filter.service";
import {getDateLabel} from "../charts";
import {DailyMeasurement, MonthlySummary, SummarizedMeasurement, SummaryData, YearlySummary} from "../../data-classes";
import HighchartsBoost from "highcharts/modules/boost"
HighchartsBoost(Highcharts)

type DetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends { details?: number[] } ? K : never
}[keyof SummarizedMeasurement]


@Component({
    selector: 'heatmap-chart',
    templateUrl: './heatmap-chart.component.html',
    styleUrls: ['./heatmap-chart.component.css']
})
export class HeatmapChart extends ChartComponentBase {
    @Input() detailProperty: DetailsProperty = "sunshineMinutes"
    @Input() abc?: (m: DailyMeasurement | MonthlySummary | YearlySummary) => number[]
    @Input() colorStops?: { value: number, color: Highcharts.ColorString }[]

    private detailedSeries?: Highcharts.Series

    constructor(filterService: FilterService) {
        super(filterService)
    }

    protected override async setChartData(summaryData: SummaryData) {
        let heatmapData: Highcharts.PointOptionsType[] = []
        if (summaryData.details) {
            summaryData.details.forEach(m => {
                let dateLabel = "dateInUtcMillis" in m ? m.dateInUtcMillis : getDateLabel(m)
                let details = this.abc!(m)
                if (details) {
                    for (let hour = 0; hour < details.length; hour++) {
                        let value = details[hour]
                        if (value != null) {
                            heatmapData.push([dateLabel, hour, details[hour]])
                        }
                    }
                }
            })
        }
        this.detailedSeries?.setData(heatmapData, false)
    }

    private queueRedrawChart() {
        setTimeout(() => this.chart?.redraw(), 0)
    }

    protected createSeries(chart: Highcharts.Chart): void {
        this.detailedSeries = chart.addSeries({
            type: 'heatmap',
            colsize: 24 * 60 * 60 * 1000,
            turboThreshold: 0,
            yAxis: "yAxisDetails",
        })
    }


    protected override getYAxes(): Highcharts.AxisOptions[] {
        return [{
            id: "yAxisDetails",
            title: {text: undefined},
            reversedStacks: false,
            min: 0,
            max: 23,
            tickInterval: 6,
            endOnTick: false,
        }]
    }

    protected override getColorAxis(): Highcharts.ColorAxisOptions | null {
        if (this.colorStops) {
            let minStopValue = Math.min.apply(null, this.colorStops.map(s => s.value))
            let maxStopValue = Math.max.apply(null, this.colorStops.map(s => s.value))
            let heatmapColorStopsRelative: [number, Highcharts.ColorString][] = this.colorStops
                .map(s => [(s.value - minStopValue) / (maxStopValue - minStopValue), s.color])
            console.log(heatmapColorStopsRelative)

            return {
                stops: heatmapColorStopsRelative,
                min: this.colorStops[0].value,
                max: this.colorStops[this.colorStops.length - 1].value
            }
        } else {
            return {minColor: 'rgb(70, 50, 80)', maxColor: 'rgb(210, 150, 240)'}
        }
    }
}
