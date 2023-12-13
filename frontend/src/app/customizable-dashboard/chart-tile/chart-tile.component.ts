import {Component, EventEmitter, Output, Type, ViewChild} from '@angular/core';
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
    // weatherStation?: WeatherStation = {
    //     "id": 128 as unknown as bigint,
    //     "name": "Freiburg",
    //     "federalState": "Baden-Württemberg",
    //     "height": 237,
    //     "latitude": 48.0232,
    //     "longitude": 7.8343
    // }
    // chartType: ChartType = {component: HeatmapChart, property: "airTemperatureCentigrade"}
    // year?: number = 2023
    @Output() weatherStationChanged = new EventEmitter<WeatherStation>()

    @ViewChild(NgComponentOutlet, {static: false}) ngComponentOutlet!: NgComponentOutlet
    // protected readonly MinAvgMaxChart = MinAvgMaxChart;
    // protected readonly WeatherStationMap = WeatherStationMap;
    // protected readonly HeatmapChart = HeatmapChart;

    constructor() {
    }

    get year(): number {
        return this.getChartComponent()?.year || 2023
    }

    set year(year: number) {
        let chartComponent = this.getChartComponent()
        if (chartComponent) {
            console.log(`setting year ${year} in chart component`)
            chartComponent.year = year
        }
    }

    weatherStationSelected(station: WeatherStation) {
        this.weatherStationChanged.emit(station)
        let chartComponent = this.getChartComponent()
        if (chartComponent) {
            console.log(`setting weather station ${station.name} in chart component`)
            chartComponent.weatherStation = station
        }
    }

    chartTypeSelected(chartType: ChartDefinition) {
        this.chartComponent = chartType.component as Type<any>
        this.inputs = {
            "property": chartType.property,
            "name": chartType.measurementName,
            "unit": chartType.unit,
        }
    }

    chartComponent: Type<any> | null = null
    inputs?: Record<string, unknown>

    private ifDefinedCreateChart() {
        // if (!this.weatherStation || !this.chartType || !this.year) {
        //     return
        // }


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
