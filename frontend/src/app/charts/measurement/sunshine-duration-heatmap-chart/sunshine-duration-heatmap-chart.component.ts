import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../../min-avg-max-chart/min-avg-max-chart.component";
import * as Highcharts from "highcharts";

@Component({
  templateUrl: './sunshine-duration-heatmap-chart.component.html',
  styleUrls: ['./sunshine-duration-heatmap-chart.component.css']
})
export class SunshineDurationHeatmapChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart

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
