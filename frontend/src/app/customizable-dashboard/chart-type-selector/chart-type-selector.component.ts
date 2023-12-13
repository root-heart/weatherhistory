import {Component, EventEmitter, Output, Type} from '@angular/core';
import {AirTemperatureChartComponent} from "../../charts/measurement/air-temperature-chart.component";
import {DewPointTemperatureChartComponent} from "../../charts/measurement/dew-point-temperature-chart.component";
import {HumidityChartComponent} from "../../charts/measurement/humidity-chart.component";
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

@Component({
    selector: 'chart-type-selector',
    templateUrl: './chart-type-selector.component.html',
    styleUrls: ['./chart-type-selector.component.css']
})
export class ChartTypeSelectorComponent {
    @Output() chartTypeSelected = new EventEmitter<Type<any>>()

    availableChartTypes: Record<string, Type<any>> = {
        "Lufttemperatur Min/Avg/Max": AirTemperatureChartComponent,
        "Luftfeuchtigkeit Min/Avg/Max": HumidityChartComponent,
        "Taupunkt Min/Avg/Max": DewPointTemperatureChartComponent,
        "Luftdruck Min/Avg/Max": AirPressureChartComponent,
        "Sichtweite Min/Avg/Max": VisibilityChartComponent,
        "Sonnenscheindauer": SunshineDurationChartComponent,
        "Lufttemperatur Details": AirTemperatureHeatmapChartComponent,
        "Sonnenschein Details": SunshineDurationHeatmapChartComponent,
        "Niederschlag": PrecipitationChartComponent
    }

    chartTypeChanged(event: Event) {
        let select = event.target as HTMLSelectElement
        let chartType = this.availableChartTypes[select.value]
        this.chartTypeSelected.emit(chartType)
    }
}
