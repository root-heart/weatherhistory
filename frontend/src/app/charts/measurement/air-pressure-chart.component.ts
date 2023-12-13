import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
  template: '<min-avg-max-chart property="airPressureHectopascals" name="Luftdruck" unit="hPa" #chart/>',
})
export class AirPressureChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart
}
