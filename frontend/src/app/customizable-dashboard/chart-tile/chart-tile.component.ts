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
import {
    AirTemperatureHeatmapChartComponent
} from "../../charts/measurement/air-temperature-heatmap-chart/air-temperature-heatmap-chart.component";
import {
    SunshineDurationHeatmapChartComponent
} from "../../charts/measurement/sunshine-duration-heatmap-chart/sunshine-duration-heatmap-chart.component";
import {RainChartComponent} from "../../charts/measurement/rain-chart.component";
import {
    CloudCoverageChartComponent
} from "../../charts/measurement/cloud-coverage-chart/cloud-coverage-chart.component";
import {SnowChartComponent} from "../../charts/measurement/snow-chart.component";
import {
    SunshineCloudCoverageHeatmapChartComponent
} from "../../charts/measurement/sunshine-cloud-coverage-heatmap-chart/sunshine-cloud-coverage-heatmap-chart.component";

addMore(Highcharts);

@Component({
    selector: 'chart-tile',
    templateUrl: './chart-tile.component.html',
    styleUrls: ['./chart-tile.component.scss']
})
export class ChartTileComponent {
    @ViewChild(NgComponentOutlet, {static: false}) ngComponentOutlet!: NgComponentOutlet

    airTemperatureSummary = {name: "Lufttemperatur Min/Avg/Max", component: AirTemperatureChartComponent};
    airTemperatureDetails = {name: "Lufttemperatur Details", component: AirTemperatureHeatmapChartComponent};
    dewPointTemperatureSummary = {name: "Taupunkt Min/Avg/Max", component: DewPointTemperatureChartComponent};
// let dewPointTemperatureDetails = {name: "Taupunkt Min/Avg/Max", component: DewPointTemperatureChartComponent};

    sunshineDurationSum = {name: "Sonnenscheindauer", component: SunshineDurationChartComponent};
    sunshineDurationDetails = {name: "Sonnenschein Details", component: SunshineDurationHeatmapChartComponent};

    rainSummary = {name: "Regen", component: RainChartComponent};
    snowSummary = {name: "Schnee", component: SnowChartComponent};

    airPressureSummary = {name: "Luftdruck Min/Avg/Max", component: AirPressureChartComponent}
    humiditySummary = {name: "Luftfeuchtigkeit Min/Avg/Max", component: HumidityChartComponent}
    visibilitySummary = {name: "Sichtweite Min/Avg/Max", component: VisibilityChartComponent}

    cloudCoverageDetails = {name: "Wolkenbedeckung", component: CloudCoverageChartComponent};

    sunshineCloudCoverageDetails = {name: "Sonne x Wolken", component: SunshineCloudCoverageHeatmapChartComponent}

    // availableChartDefinitions: { name: string, definitions: ChartDefinition[] }[] = [
    //     {name: "Temperatur", definitions: [this.airTemperatureSummary, this.airTemperatureDetails, this.dewPointTemperatureSummary]},
    //     {name: "Sonnenschein", definitions: [this.sunshineDurationSum, this.sunshineDurationDetails]},
    //     {name: "Niederschlag", definitions: [rainSummary, snowSummary]},
    //     {name: "Bewölkung", definitions: [cloudCoverageDetails]},
    //     {name: "kombinierte Werte", definitions: [sunshineCloudCoverageDetails]}
    // ]


    constructor(private changeDetector: ChangeDetectorRef) {
    }

    private _chartDefinition: ChartDefinition = this.airTemperatureSummary

    get chartDefinition(): ChartDefinition {
        return this._chartDefinition
    }

    set chartDefinition(chartDefinition: ChartDefinition) {
        if (this._chartDefinition != chartDefinition) {
            console.log(`set chart definition to ${chartDefinition.name}`)
            this._chartDefinition = chartDefinition
            this.updateChartComponent()
        }
    }

    private _year: number = new Date().getFullYear()

    get year(): number {
        return this._year
    }

    set year(year: number) {
        if (this._year != year) {
            console.log(`set year to ${year}`)
            this._year = year
            this.updateChartComponent()
        }
    }

    private _weatherStation?: WeatherStation

    set weatherStation(station: WeatherStation) {
        if (this._weatherStation != station) {
            console.log(`set weather station to ${station.name}`)
            this._weatherStation = station
            this.updateChartComponent()
        }
    }

    weatherStationSelected(station: WeatherStation) {
        this.weatherStation = station
    }

    getChartDefinitionName(chartDefinition: ChartDefinition) {
        return chartDefinition.name
    }

    private updateChartComponent() {
        console.log("triggering change detector")
        this.changeDetector.detectChanges()
        if (this._weatherStation && this._year) {
            let chartComponent = this.getChartComponent()
            if (chartComponent) {
                console.log(`setting station ${this._weatherStation?.name} and year ${this._year} in chart component`)
                chartComponent.update(this._weatherStation, this._year)
            }
        }
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

