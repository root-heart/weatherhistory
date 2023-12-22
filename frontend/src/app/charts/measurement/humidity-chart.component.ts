import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
    template: `
        <min-avg-max-chart measurementName="humidity" name="Luftfeuchtigkeit" unit="%" #chart/>`,
    styles: [`min-avg-max-chart {
        --highcharts-color-0: hsla(220, 80%, 30%, 50%);
        --highcharts-color-1: hsl(220, 80%, 30%);
    }`]
})
export class HumidityChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart
}
