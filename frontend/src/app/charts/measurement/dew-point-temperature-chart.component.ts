import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
    template: `<min-avg-max-chart property="dewPointTemperatureCentigrade" name="Taupunkttemperatur" unit="°C" #chart/>`,
    styles: [`min-avg-max-chart {
        --highcharts-color-0: hsla(220, 80%, 70%, 50%);
        --highcharts-color-1: hsl(220, 80%, 70%);
    }`]
})
export class DewPointTemperatureChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart
}
