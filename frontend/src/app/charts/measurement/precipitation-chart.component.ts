import {Component, ViewChild} from '@angular/core';
import {SumChartComponent} from "../sum-chart/sum-chart.component";

@Component({
    template: `
        <sum-chart #chart sumProperty="rainfallMillimeters" sum2Property="snowfallMillimeters"
                   name='Niederschlag' unit='mm'/>
    `
})
export class PrecipitationChartComponent {
    @ViewChild("chart") chart!: SumChartComponent

}
