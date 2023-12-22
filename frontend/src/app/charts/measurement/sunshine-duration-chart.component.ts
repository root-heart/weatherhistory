import {Component, ViewChild} from '@angular/core';
import {SumChartComponent} from "../sum-chart/sum-chart.component";
import * as Highcharts from "highcharts";

@Component({
    template: `
        <sum-chart #chart measurementName="sunshine" name='Sonnenschein' unit='h'
                   [yAxisLabelFormatter]='yAxisMinutesAsHour' [valueTooltipFormatter]='formatAsHour'/>`,
    styles: [`sum-chart {
        --highcharts-color-0: rgb(238, 170, 34);
    }`]
})
export class SunshineDurationChartComponent {
    @ViewChild("chart") chart!: SumChartComponent

    formatAsHour(value: number): string {
        return formatAsHour(value)
    }

    yAxisMinutesAsHour(x: Highcharts.AxisLabelsFormatterContextObject): string {
        // CAUTION! Highcharts does some crappy stuff, so that `this` is no instance of the enclosing class here...
        return formatAsHour(x.value as number)
    }
}

// TODO DRY
function formatAsHour(value: number): string {
    let hours = Math.floor(value / 60)
    let minutes = (value % 60).toString().padStart(2, '0')
    return `${hours}:${minutes}`
}

