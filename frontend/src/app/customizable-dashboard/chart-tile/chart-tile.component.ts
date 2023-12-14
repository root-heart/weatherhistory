import {ChangeDetectorRef, Component, Type, ViewChild} from '@angular/core';
import {WeatherStation} from "../../WeatherStationService";
import {ChartComponentBase} from "../../charts/chart-component-base";

import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {NgComponentOutlet} from "@angular/common";
import {AirTemperatureChartComponent} from "../../charts/measurement/air-temperature-chart.component";
import {HumidityChartComponent} from "../../charts/measurement/humidity-chart.component";
import {DewPointTemperatureChartComponent} from "../../charts/measurement/dew-point-temperature-chart.component";
import {AirPressureChartComponent} from "../../charts/measurement/air-pressure-chart.component";
import {VisibilityChartComponent} from "../../charts/measurement/visibility-chart.component";
import {SunshineDurationChartComponent} from "../../charts/measurement/sunshine-duration-chart.component";
import {
    AirTemperatureHeatmapChartComponent
} from "../../charts/measurement/air-temperature-heatmap-chart/air-temperature-heatmap-chart.component";
import {
    SunshineDurationHeatmapChartComponent
} from "../../charts/measurement/sunshine-duration-heatmap-chart/sunshine-duration-heatmap-chart.component";
import {PrecipitationChartComponent} from "../../charts/measurement/precipitation-chart.component";

addMore(Highcharts);

@Component({
    selector: 'chart-tile',
    templateUrl: './chart-tile.component.html',
    styleUrls: ['./chart-tile.component.css']
})
export class ChartTileComponent {
    @ViewChild(NgComponentOutlet, {static: false}) ngComponentOutlet!: NgComponentOutlet

    availableChartDefinitions: ChartDefinition[] = [
        {name: "Lufttemperatur Min/Avg/Max", component: AirTemperatureChartComponent},
        {name: "Luftfeuchtigkeit Min/Avg/Max", component: HumidityChartComponent},
        {name: "Taupunkt Min/Avg/Max", component: DewPointTemperatureChartComponent},
        {name: "Luftdruck Min/Avg/Max", component: AirPressureChartComponent},
        {name: "Sichtweite Min/Avg/Max", component: VisibilityChartComponent},
        {name: "Sonnenscheindauer", component: SunshineDurationChartComponent},
        {name: "Lufttemperatur Details", component: AirTemperatureHeatmapChartComponent},
        {name: "Sonnenschein Details", component: SunshineDurationHeatmapChartComponent},
        {name: "Niederschlag", component: PrecipitationChartComponent},
    ]

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    private _chartDefinition: ChartDefinition = this.availableChartDefinitions[0]

    get chartDefinition(): ChartDefinition {
        return this._chartDefinition
    }

    set chartDefinition(d: ChartDefinition) {
        this._chartDefinition = d
        this.updateChartComponent()
    }

    private _year?: number

    get year(): number {
        return this._year || 2023
    }

    set year(year: number) {
        this._year = year
        this.updateChartComponent()
    }

    private _weatherStation?: WeatherStation

    set weatherStation(station: WeatherStation) {
        this._weatherStation = station
        this.updateChartComponent()
    }

    weatherStationSelected(station: WeatherStation) {
        this.weatherStation = station
    }

    private updateChartComponent() {
        this.changeDetector.detectChanges()
        if (this._weatherStation && this._year) {
            let chartComponent = this.getChartComponent()
            if (chartComponent) {
                console.log(`setting station ${this._weatherStation?.name} and year ${this._year} in chart component`)
                chartComponent.update(this._weatherStation, this._year)
            }
        }
    }

    private getChartComponent(): ChartComponentBase | null {
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
            return instance.chart as ChartComponentBase
        }
        return null
    }
}

export type ChartDefinition = {
    name: string,
    component: Type<any>
}

