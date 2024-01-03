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
import {
    ChartConfiguration,
    ChartConfigurationDialog,
    ChartType,
    Measurement,
} from "../../chart-configuration-dialog/chart-configuration-dialog.component"
import {
    AirTemperatureHeatmapChartComponent
} from "../../charts/measurement/air-temperature-heatmap-chart/air-temperature-heatmap-chart.component";
import {WindDirectionChart} from "../../charts/wind-direction-chart/wind-direction-chart.component";
import {
    SunshineDurationHeatmapChartComponent
} from "../../charts/measurement/sunshine-duration-heatmap-chart/sunshine-duration-heatmap-chart.component";

addMore(Highcharts);

const chartComponents: {measurementName: Measurement, chartType: ChartType, component: Type<any>}[] = [
    {measurementName: Measurement.airTemperature, chartType: ChartType.daily, component: AirTemperatureChartComponent},
    {measurementName: Measurement.airTemperature, chartType: ChartType.details, component: AirTemperatureHeatmapChartComponent},
    {measurementName: Measurement.airPressure, chartType: ChartType.daily, component: AirPressureChartComponent},
    {measurementName: Measurement.humidity, chartType: ChartType.daily, component: HumidityChartComponent},
    {measurementName: Measurement.dewPoint, chartType: ChartType.daily, component: DewPointTemperatureChartComponent},
    {measurementName: Measurement.sunshine, chartType: ChartType.daily, component: SunshineDurationChartComponent},
    {measurementName: Measurement.sunshine, chartType: ChartType.details, component: SunshineDurationHeatmapChartComponent},
    {measurementName: Measurement.rain, chartType: ChartType.daily, component: RainChartComponent},
    {measurementName: Measurement.snow, chartType: ChartType.daily, component: SnowChartComponent},
    {measurementName: Measurement.visibility, chartType: ChartType.daily, component: VisibilityChartComponent},
    {measurementName: Measurement.cloudCoverage, chartType: ChartType.details, component: CloudCoverageChartComponent},
    {measurementName: Measurement.windDirection, chartType: ChartType.daily, component: WindDirectionChart},
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
    @ViewChild(ChartConfigurationDialog) chartConfigurationDialog!: ChartConfigurationDialog

    chartComponentType: Type<any> | undefined

    private year?: number
    private measurement?: Measurement
    private weatherStation?: WeatherStation

    get title(): string | undefined {
        if (this.measurement && this.weatherStation && this.year) {
            return `${this.measurement.name} | ${this.weatherStation.name} | ${this.year}`
        }
        return undefined
    }

    updateChartComponent(weatherStation?: WeatherStation, measurement?: Measurement, chartType?: ChartType, year?: number) {
        if (weatherStation === undefined || measurement === undefined || chartType === undefined || year === undefined) {
            return
        }
        this.chartComponentType = chartComponents.find(v => v.measurementName === measurement && v.chartType === chartType)?.component
        console.log(`determined chart component ${this.chartComponentType?.name} for measurement ${measurement.name} and chart type ${chartType.name}`)
        setTimeout(() => {
            let chartComponentInstance = this.getChartComponentInstance()
            if (chartComponentInstance) {
                console.log(`setting station ${weatherStation?.name} and year ${year} in chart component`)
                this.weatherStation = weatherStation
                this.measurement = measurement
                this.year = year
                chartComponentInstance.update(this.weatherStation, this.year)
            }
        })
    }

    openChartConfigurationDialog() {
        this.chartConfigurationDialog.show(
            this.weatherStation,
            this.measurement,
            this.year
        )
    }

    chartConfigurationConfirmed(chartConfig: ChartConfiguration) {
        this.updateChartComponent(
            chartConfig.station, chartConfig.measurement, chartConfig.chartType, chartConfig.year)
    }

    reflowChart() {
        this.getChartComponentInstance()
            ?.reflowChart()
    }

    // TODO fix this name clash with the member chartComponent
    private getChartComponentInstance(): ChartBaseComponent<any> | null {
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

