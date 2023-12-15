import {Component, ViewChild} from '@angular/core';
import * as Highcharts from "highcharts";
import {HeatmapChart} from "../../heatmap-chart/heatmap-chart.component";

@Component({
    templateUrl: './air-temperature-heatmap-chart.component.html'
})
export class AirTemperatureHeatmapChartComponent {
    @ViewChild("chart") chart!: HeatmapChart

    // TODO DRY
    yAxisHours(x: Highcharts.AxisLabelsFormatterContextObject): string {
        return formatAsHour(x.value as number * 60)
    }

}

// TODO DRY
function formatAsHour(value: number): string {
    let hours = Math.floor(value / 60)
    let minutes = (value % 60).toString().padStart(2, '0')
    return `${hours}:${minutes}`
}
