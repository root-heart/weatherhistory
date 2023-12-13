import {ChangeDetectorRef, Component, EventEmitter, Output, Type, ViewChild} from '@angular/core';
import {WeatherStation} from "../../WeatherStationService";
import {ChartComponentBase} from "../../charts/chart-component-base";
import {SummarizedMeasurement, SummaryData} from "../../data-classes";
import {MinAvgMaxChart} from "../../charts/min-avg-max-chart/min-avg-max-chart.component";
import {FilterService} from "../../filter.service";
import {WeatherStationMap} from "../../weather-station-map/weather-station-map.component";
import {HeatmapChart} from "../../charts/heatmap-chart/heatmap-chart.component";

import * as Highcharts from 'highcharts';
import addMore from "highcharts/highcharts-more";
import {NgComponentOutlet} from "@angular/common";

addMore(Highcharts);

@Component({
    selector: 'chart-tile',
    templateUrl: './chart-tile.component.html',
    styleUrls: ['./chart-tile.component.css']
})
export class ChartTileComponent {
    @ViewChild(NgComponentOutlet, {static: false}) ngComponentOutlet!: NgComponentOutlet

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    private _year?: number
    get year(): number {
        return this.getChartComponent()?.year || 2023
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

    chartTypeSelected(chartType: ChartDefinition) {
        this.chartComponent = chartType.component as Type<any>
        this.inputs = {
            "property": chartType.property,
            "name": chartType.measurementName,
            "unit": chartType.unit,
        }
        this.updateChartComponent()
    }

    chartComponent: Type<any> | null = null
    inputs?: Record<string, unknown>

    private updateChartComponent() {
        this.changeDetector.detectChanges()
        let chartComponent = this.getChartComponent()
        if (chartComponent) {
            console.log(`setting station ${this._weatherStation?.name} and year ${this._year} in chart component`)
            if (this._weatherStation) chartComponent.weatherStation = this._weatherStation
            if (this._year) chartComponent.year = this._year
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
        return componentRef['instance'] as ChartComponentBase
    }
}

export type ChartDefinition = {
    component: typeof ChartComponentBase
    property: keyof SummarizedMeasurement
    measurementName: string
    unit: string
}
