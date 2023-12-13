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
import {AirTemperatureChartComponent} from "../../charts/measurement/air-temperature-chart.component";

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

    chartTypeSelected(chartType: Type<any>) {
        this.chartComponent = chartType
        this.updateChartComponent()
    }

    chartComponent: Type<any> | null = null

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
