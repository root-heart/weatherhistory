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
import {PrecipitationChartComponent} from "../../charts/measurement/precipitation-chart.component";
import {
    CloudCoverageChartComponent
} from "../../charts/measurement/cloud-coverage-chart/cloud-coverage-chart.component";

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
        {name: "Lufttemperatur Details", component: AirTemperatureHeatmapChartComponent},
        {name: "Sonnenscheindauer", component: SunshineDurationChartComponent},
        {name: "Sonnenschein Details", component: SunshineDurationHeatmapChartComponent},
        {name: "Wolkenbedeckung", component: CloudCoverageChartComponent},
        {name: "Niederschlag", component: PrecipitationChartComponent},
        {name: "Luftdruck Min/Avg/Max", component: AirPressureChartComponent},
        {name: "Luftfeuchtigkeit Min/Avg/Max", component: HumidityChartComponent},
        {name: "Taupunkt Min/Avg/Max", component: DewPointTemperatureChartComponent},
        {name: "Sichtweite Min/Avg/Max", component: VisibilityChartComponent},
    ]

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    private _chartDefinition: ChartDefinition = this.availableChartDefinitions[0]

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

    private getChartComponent(): ChartBaseComponent | null {
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
            return instance.chart as ChartBaseComponent
        }
        return null
    }
}

export type ChartDefinition = {
    name: string,
    component: Type<any>
}

