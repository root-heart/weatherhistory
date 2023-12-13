import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
    template: '<min-avg-max-chart property="airTemperatureCentigrade" name="Lufttemperatur" unit="°C" #chart/>'
})
export class AirTemperatureChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart
}
