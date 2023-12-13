import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
  template: '<min-avg-max-chart property="humidityPercent" name="Luftfeuchtigkeit" unit="%" #chart/>',
})
export class HumidityChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart
}
