import {ChangeDetectorRef, Component, Type, ViewChild} from '@angular/core';
import {WeatherStation} from "../../WeatherStationService";
import {ChartBaseComponent} from "../../charts/chart-base.component";

import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {NgComponentOutlet} from "@angular/common";
import {AirTemperatureChartComponent} from "../../charts/measurement/air-temperature-chart.component";
import {HumidityChartComponent} from "../../charts/measurement/humidity-chart.component";
import {DewPointTemperatureChartComponent} from "../../charts/measurement/dew-point-temperature-chart.component";
import {AirPressureChartComponent} from "../../charts/measurement/air-pressure-chart.component";
import {VisibilityChartComponent} from "../../charts/measurement/visibility-chart.component";
import {SunshineDurationChartComponent} from "../../charts/measurement/sunshine-duration-chart.component";
import {RainChartComponent} from "../../charts/measurement/rain-chart.component";
import {
    CloudCoverageChartComponent
} from "../../charts/measurement/cloud-coverage-chart/cloud-coverage-chart.component";
import {SnowChartComponent} from "../../charts/measurement/snow-chart.component";
import {airTemperature, MeasurementName} from "../../chart-configuration-dialog/chart-configuration-dialog.component";

addMore(Highcharts);


@Component({
    selector: 'chart-tile',
    templateUrl: './chart-tile.component.html',
    styleUrls: ['./chart-tile.component.scss']
})
export class ChartTileComponent {
    @ViewChild(NgComponentOutlet, {static: false}) ngComponentOutlet!: NgComponentOutlet

    chartComponent: Type<any> = AirTemperatureChartComponent
//     airTemperatureSummary = {name: "Lufttemperatur Min/Avg/Max", component: AirTemperatureChartComponent};
//     airTemperatureDetails = {name: "Lufttemperatur Details", component: AirTemperatureHeatmapChartComponent};
//     dewPointTemperatureSummary = {name: "Taupunkt Min/Avg/Max", component: DewPointTemperatureChartComponent};
// // let dewPointTemperatureDetails = {name: "Taupunkt Min/Avg/Max", component: DewPointTemperatureChartComponent};
//
//     sunshineDurationSum = {name: "Sonnenscheindauer", component: SunshineDurationChartComponent};
//     sunshineDurationDetails = {name: "Sonnenschein Details", component: SunshineDurationHeatmapChartComponent};
//
//     rainSummary = {name: "Regen", component: RainChartComponent};
//     snowSummary = {name: "Schnee", component: SnowChartComponent};
//
//     airPressureSummary = {name: "Luftdruck Min/Avg/Max", component: AirPressureChartComponent}
//     humiditySummary = {name: "Luftfeuchtigkeit Min/Avg/Max", component: HumidityChartComponent}
//     visibilitySummary = {name: "Sichtweite Min/Avg/Max", component: VisibilityChartComponent}
//
//     cloudCoverageDetails = {name: "Wolkenbedeckung", component: CloudCoverageChartComponent};
//
//     sunshineCloudCoverageDetails = {name: "Sonne x Wolken", component: SunshineCloudCoverageHeatmapChartComponent}

    // availableChartDefinitions: { name: string, definitions: ChartDefinition[] }[] = [
    //     {name: "Temperatur", definitions: [this.airTemperatureSummary, this.airTemperatureDetails, this.dewPointTemperatureSummary]},
    //     {name: "Sonnenschein", definitions: [this.sunshineDurationSum, this.sunshineDurationDetails]},
    //     {name: "Niederschlag", definitions: [rainSummary, snowSummary]},
    //     {name: "Bewölkung", definitions: [cloudCoverageDetails]},
    //     {name: "kombinierte Werte", definitions: [sunshineCloudCoverageDetails]}
    // ]

    private _year: number = new Date().getFullYear()

    get year() {
        return this._year
    }

    private _measurementName: MeasurementName = airTemperature

    get measurementName() {
        return this._measurementName
    }

    private _weatherStation?: WeatherStation

    get weatherStation() {
        return this._weatherStation
    }

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    updateChartComponent(weatherStation: WeatherStation, measurementName: MeasurementName, year: number) {
        if (measurementName === "Luftdruck") {
            this.chartComponent = AirPressureChartComponent
        } else if (measurementName === "Luftfeuchtigkeit") {
            this.chartComponent = HumidityChartComponent
        } else if (measurementName === "Taupunkttemperatur") {
            this.chartComponent = DewPointTemperatureChartComponent
        } else if (measurementName === "Sonnenschein") {
            this.chartComponent = SunshineDurationChartComponent
        } else if (measurementName === "Windrichtung") {
        } else if (measurementName === "Windgeschwindigkeit") {
        } else if (measurementName === "Wolkenuntergrenze (WIP)") {
        } else if (measurementName === "Regen") {
            this.chartComponent = RainChartComponent
        } else if (measurementName === "Schnee") {
            this.chartComponent = SnowChartComponent
        } else if (measurementName === "Sichtweite") {
            this.chartComponent = VisibilityChartComponent
        } else if (measurementName === "Lufttemperatur") {
            this.chartComponent = AirTemperatureChartComponent
        } else if (measurementName === "Bedeckungsgrad") {
            this.chartComponent = CloudCoverageChartComponent
        }
        console.log("triggering change detector")
        this.changeDetector.detectChanges()
        setTimeout(() => {
            let chartComponent = this.getChartComponent()
            if (chartComponent) {
                console.log(`setting station ${weatherStation.name} and year ${year} in chart component`)
                this._weatherStation = weatherStation
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

