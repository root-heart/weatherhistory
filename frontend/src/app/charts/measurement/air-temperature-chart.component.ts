import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
    template: '<min-avg-max-chart property="airTemperatureCentigrade" name="Lufttemperatur" unit="°C" #chart/>',
    styles: [`min-avg-max-chart {
        --highcharts-color-0: #b007;
        --highcharts-color-1: #b00;
    }`]
})
export class AirTemperatureChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart
}
