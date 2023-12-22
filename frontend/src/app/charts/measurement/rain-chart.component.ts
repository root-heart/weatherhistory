import {Component, ViewChild} from '@angular/core';
import {SumChartComponent} from "../sum-chart/sum-chart.component";

@Component({
    template: `
        <sum-chart #chart measurementName="rain" name='Regen' unit='mm'/>`,
    styles: [`sum-chart {
        --highcharts-color-0: hsl(210, 80%, 50%);
        --highcharts-color-1: hsl(210, 90%, 95%);
    }`]
})
export class RainChartComponent {
    @ViewChild("chart") chart!: SumChartComponent

}
