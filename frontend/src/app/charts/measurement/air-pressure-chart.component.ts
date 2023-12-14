import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
    template: `<min-avg-max-chart property="airPressureHectopascals" name="Luftdruck" unit="hPa" #chart/>`,
    styles: [`min-avg-max-chart {
        --highcharts-color-0: hsla(120, 80%, 45%, 50%);
        --highcharts-color-1: hsl(120, 80%, 45%);
    }`]
})
export class AirPressureChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart
}
