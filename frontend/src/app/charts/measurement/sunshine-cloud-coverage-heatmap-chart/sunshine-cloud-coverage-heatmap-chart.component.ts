import {Component, ViewChild} from '@angular/core';
import {HeatmapChart} from "../../heatmap-chart/heatmap-chart.component";

@Component({
  selector: 'app-sunshine-cloud-coverage-heatmap-chart',
  templateUrl: './sunshine-cloud-coverage-heatmap-chart.component.html',
  styleUrls: ['./sunshine-cloud-coverage-heatmap-chart.component.css']
})
export class SunshineCloudCoverageHeatmapChartComponent {
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
