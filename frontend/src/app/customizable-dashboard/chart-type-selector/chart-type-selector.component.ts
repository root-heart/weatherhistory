import {Component, EventEmitter, Output} from '@angular/core';
import {ChartComponentBase} from "../../charts/chart-component-base";
import {MinAvgMaxChart} from "../../charts/min-avg-max-chart/min-avg-max-chart.component";
import {HeatmapChart} from "../../charts/heatmap-chart/heatmap-chart.component";
import {ChartDefinition} from "../chart-tile/chart-tile.component";

@Component({
    selector: 'chart-type-selector',
    templateUrl: './chart-type-selector.component.html',
    styleUrls: ['./chart-type-selector.component.css']
})
export class ChartTypeSelectorComponent {
    @Output() chartTypeSelected = new EventEmitter<ChartDefinition>()

    availableChartTypes: { name: string, type: ChartDefinition }[] = [
        {
            name: "Lufttemperatur Min/Avg/Max",
            type: {
                component: MinAvgMaxChart,
                property: "airTemperatureCentigrade",
                measurementName: "Lufttemperatur",
                unit: "°C"
            }
        },
        // {name: "Lufttemperatur Heatmap", type: {component: HeatmapChart, property: "airTemperatureCentigrade"}}
        {
            name: "Luftfeuchtigkeit Min/Avg/Max",
            type: {
                component: MinAvgMaxChart,
                property: "humidityPercent",
                measurementName: "Luftfeuchtigkeit",
                unit: "%"
            }
        },
        {
            name: "Taupunkt Min/Avg/Max",
            type: {
                component: MinAvgMaxChart,
                property: "dewPointTemperatureCentigrade",
                measurementName: "Taupunkttemperatur",
                unit: "°C"
            }
        },
        {
            name: "Luftdruck Min/Avg/Max",
            type: {
                component: MinAvgMaxChart,
                property: "airPressureHectopascals",
                measurementName: "Luftdruck",
                unit: "hPa"
            }
        },
        {
            name: "Sichtweite Min/Avg/Max",
            type: {component: MinAvgMaxChart, property: "visibilityMeters", measurementName: "Sichtweite", unit: "m"}
        },
    ]

    chartTypeChanged(event: Event) {
        let select = event.target as HTMLSelectElement
        let chartType = this.availableChartTypes
            .filter(c => c.name === select.value)[0]
            .type
        this.chartTypeSelected.emit(chartType)
    }
}
