import {Component, ViewChild} from '@angular/core';
import {HeatmapChart} from "../../heatmap-chart/heatmap-chart.component";

@Component({
  selector: 'app-cloud-coverage-chart',
  templateUrl: './cloud-coverage-chart.component.html',
  styleUrls: ['./cloud-coverage-chart.component.css']
})
export class CloudCoverageChartComponent {
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
