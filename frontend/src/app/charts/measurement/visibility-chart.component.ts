import {Component, ViewChild} from '@angular/core';
import {MinAvgMaxChart} from "../min-avg-max-chart/min-avg-max-chart.component";

@Component({
    template: '<min-avg-max-chart  property="visibilityMeters" name="Sichtweite" unit="m" #chart/>',
})
export class VisibilityChartComponent {
    @ViewChild("chart") chart!: MinAvgMaxChart

}
