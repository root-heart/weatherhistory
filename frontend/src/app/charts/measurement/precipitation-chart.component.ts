import {Component, ViewChild} from '@angular/core';
import {SumChartComponent} from "../sum-chart/sum-chart.component";

@Component({
    template: `
        <sum-chart #chart sumProperty="rainfallMillimeters" sum2Property="snowfallMillimeters"
                   name='Niederschlag' unit='mm'/>`,
    styles: [`sum-chart {
        --highcharts-color-0: hsl(210, 80%, 50%);
        --highcharts-color-1: hsl(210, 90%, 95%);
    }`]
})
export class PrecipitationChartComponent {
    @ViewChild("chart") chart!: SumChartComponent

}
