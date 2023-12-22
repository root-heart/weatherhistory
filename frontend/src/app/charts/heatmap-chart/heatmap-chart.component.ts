import {Component, Input} from '@angular/core';
import {ChartBaseComponent} from "../chart-base.component";
import * as Highcharts from "highcharts";
import {SummarizedMeasurement} from "../../data-classes";

type DetailsProperty = {
    [K in keyof SummarizedMeasurement]: SummarizedMeasurement[K] extends { details?: number[] } ? K : never
}[keyof SummarizedMeasurement]


@Component({
    selector: 'heatmap-chart',
    templateUrl: './heatmap-chart.component.html',
    styleUrls: ['./heatmap-chart.component.css']
})
export class HeatmapChart extends ChartBaseComponent<[number, number[]]> {
    @Input() detailProperty: DetailsProperty = "sunshineMinutes"
    @Input() colorStops: { value: number, color: Highcharts.ColorString }[] = [
        {value: 0, color: 'rgb(70, 50, 80)'},
        {value: 100, color: 'rgb(210, 150, 240)'}]

    protected override async setChartData(data: [number, number[]][]) {
        let heatmapData: Highcharts.PointOptionsType[] = []
        data.forEach(d => {
            let dateLabel = d[0]
            let details = d[1]
            if (details) {
                for (let hour = 0; hour < details.length; hour++) {
                    let value = details[hour]
                    if (value != null) {
                        heatmapData.push([dateLabel, hour + 0.5, details[hour]])
                    }
                }
            }
        })
        this.chart?.series[0].setData(heatmapData, false)
    }

    protected override createSeries(): Highcharts.SeriesOptionsType[] {
        return [{
            type: 'heatmap',
            colsize: 24 * 60 * 60 * 1000,
            turboThreshold: 0
        }]
    }

    protected override getYAxes(): Highcharts.AxisOptions[] {
        return [{
            id: "yAxisDetails",
            title: {text: undefined},
            reversedStacks: false,
            min: 0.5,
            max: 23.5,
            tickInterval: 6,
            startOnTick: false,
            endOnTick: false,
        }]
    }

    // TODO currently this needs to be executed manually AFTER colorStops are set. Perhaps this can be made a bit nicer
    protected override getColorAxis(): Highcharts.ColorAxisOptions | undefined {
        let minStopValue = Math.min.apply(null, this.colorStops.map(s => s.value))
        let maxStopValue = Math.max.apply(null, this.colorStops.map(s => s.value))
        let heatmapColorStopsRelative: [number, Highcharts.ColorString][] = this.colorStops
            .map(s => [(s.value - minStopValue) / (maxStopValue - minStopValue), s.color])
        return {
            id: `colorAxis_${this.detailProperty}`,
            stops: heatmapColorStopsRelative,
            min: this.colorStops[0].value,
            max: this.colorStops[this.colorStops.length - 1].value,
            startOnTick: false,
            endOnTick: false
        }
    }

    // TODO DRY somehow
    protected override getTooltipText(_: Highcharts.Tooltip): string {
        // there is some unexplainable (at least to me) TypeScript/JavaScript magic happening here, where 'this' is an
        // object containing the members
        // color, colorIndex, key, percentage, point, series, total, x, y
        // beware: 'this' is not a reference to the enclosing class!!
        // beware 2: that means that no method from this class can be called from this method
        // @ts-ignore
        let tooltipInformation = this as TooltipInformation
        let point = tooltipInformation.point
        let series = point.series
        let date = new Date(point.x)
        let dateString = date.toLocaleDateString("de-DE", {day: "2-digit", month: "2-digit", year: "numeric"})
        return `<b>${series.name}</b><br>`
            + `${dateString} ${point.y - 0.5}:00 Uhr: ${point.value} ${series.tooltipOptions.valueSuffix}`

    }
}
