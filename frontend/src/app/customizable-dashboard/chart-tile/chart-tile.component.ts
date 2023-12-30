import {ChangeDetectorRef, Component, Type, ViewChild} from '@angular/core'
import {WeatherStation} from "../../WeatherStationService"
import {ChartBaseComponent} from "../../charts/chart-base.component"

import * as Highcharts from 'highcharts'
import addMore from "highcharts/highcharts-more"
import {NgComponentOutlet} from "@angular/common"
import {AirTemperatureChartComponent} from "../../charts/measurement/air-temperature-chart.component"
import {HumidityChartComponent} from "../../charts/measurement/humidity-chart.component"
import {DewPointTemperatureChartComponent} from "../../charts/measurement/dew-point-temperature-chart.component"
import {AirPressureChartComponent} from "../../charts/measurement/air-pressure-chart.component"
import {VisibilityChartComponent} from "../../charts/measurement/visibility-chart.component"
import {SunshineDurationChartComponent} from "../../charts/measurement/sunshine-duration-chart.component"
import {RainChartComponent} from "../../charts/measurement/rain-chart.component"
import {CloudCoverageChartComponent} from "../../charts/measurement/cloud-coverage-chart/cloud-coverage-chart.component"
import {SnowChartComponent} from "../../charts/measurement/snow-chart.component"
import {ChartType, MeasurementName,} from "../../chart-configuration-dialog/chart-configuration-dialog.component"
import {
    AirTemperatureHeatmapChartComponent
} from "../../charts/measurement/air-temperature-heatmap-chart/air-temperature-heatmap-chart.component";
import {WindDirectionChart} from "../../charts/wind-direction-chart/wind-direction-chart.component";
import {
    SunshineDurationHeatmapChartComponent
} from "../../charts/measurement/sunshine-duration-heatmap-chart/sunshine-duration-heatmap-chart.component";

addMore(Highcharts);

const chartComponents: {measurementName: MeasurementName, chartType: ChartType, component: Type<any>}[] = [
    {measurementName: MeasurementName.airTemperature, chartType: ChartType.daily, component: AirTemperatureChartComponent},
    {measurementName: MeasurementName.airTemperature, chartType: ChartType.details, component: AirTemperatureHeatmapChartComponent},
    {measurementName: MeasurementName.airPressure, chartType: ChartType.daily, component: AirPressureChartComponent},
    {measurementName: MeasurementName.humidity, chartType: ChartType.daily, component: HumidityChartComponent},
    {measurementName: MeasurementName.dewPoint, chartType: ChartType.details, component: DewPointTemperatureChartComponent},
    {measurementName: MeasurementName.sunshine, chartType: ChartType.daily, component: SunshineDurationChartComponent},
    {measurementName: MeasurementName.sunshine, chartType: ChartType.details, component: SunshineDurationHeatmapChartComponent},
    {measurementName: MeasurementName.rain, chartType: ChartType.daily, component: RainChartComponent},
    {measurementName: MeasurementName.snow, chartType: ChartType.daily, component: SnowChartComponent},
    {measurementName: MeasurementName.visibility, chartType: ChartType.daily, component: VisibilityChartComponent},
    {measurementName: MeasurementName.cloudCoverage, chartType: ChartType.details, component: CloudCoverageChartComponent},
    {measurementName: MeasurementName.windDirection, chartType: ChartType.daily, component: WindDirectionChart},
    // TODO {measurementName: cloudBase, chartType: daily, component: ?}
    // TODO {measurementName: windSpeed, chartType: daily, component: ?}
]

@Component({
    selector: 'chart-tile',
    templateUrl: './chart-tile.component.html',
    styleUrls: ['./chart-tile.component.scss']
})
export class ChartTileComponent {
    @ViewChild(NgComponentOutlet, {static: false}) ngComponentOutlet!: NgComponentOutlet

    chartComponent: Type<any> | undefined = AirTemperatureChartComponent

    private _year: number = new Date().getFullYear()

    get year() {
        return this._year
    }

    private _measurementName = MeasurementName.airTemperature

    get measurementName() {
        return this._measurementName
    }

    private _weatherStation?: WeatherStation

    get weatherStation() {
        return this._weatherStation
    }

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    updateChartComponent(weatherStation: WeatherStation, measurementName: MeasurementName, chartType: ChartType, year: number) {
        this.chartComponent = chartComponents.find(v => v.measurementName === measurementName && v.chartType === chartType)?.component
        setTimeout(() => {
            let chartComponent = this.getChartComponent()
            if (chartComponent) {
                console.log(`setting station ${weatherStation.name} and year ${year} in chart component`)
                this._weatherStation = weatherStation
                this._measurementName = measurementName
                this._year = year
                chartComponent.update(weatherStation, year)
            }
        })
    }

    private getChartComponent(): ChartBaseComponent<any> | null {
        let ngComponentOutlet = this.ngComponentOutlet;
        if (!ngComponentOutlet) {
            return null
        }

        let componentRef = ngComponentOutlet['_componentRef']
        if (!componentRef) {
            return null
        }
        let instance = componentRef['instance'];
        if ("chart" in instance) {
            return instance.chart as ChartBaseComponent<any>
        }
        return null
    }
}

export type ChartDefinition = {
    name: string,
    component: Type<any>
}

