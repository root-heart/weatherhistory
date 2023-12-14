import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
    template: `<min-avg-max-chart  property="visibilityMeters" name="Sichtweite" unit="m" #chart/>`,
    styles: [`min-avg-max-chart {
        --highcharts-color-0: hsla(190, 20%, 90%, 50%);
        --highcharts-color-1: hsl(190, 20%, 90%);
    }`]
})
export class VisibilityChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart

}
