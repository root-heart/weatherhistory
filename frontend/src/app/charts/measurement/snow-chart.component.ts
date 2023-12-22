import {Component, ViewChild} from '@angular/core';
import {SumChartComponent} from "../sum-chart/sum-chart.component";

@Component({
    template: `
        <sum-chart #chart measurementName="snow" name='Regen' unit='mm'/>`,
    styles: [`sum-chart {
        --highcharts-color-0: hsl(210, 90%, 95%);
    }`]
})
export class SnowChartComponent {
    @ViewChild("chart") chart!: SumChartComponent

}
