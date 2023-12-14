import {Component, EventEmitter, Input, Output, Type} from '@angular/core';
import {ChartDefinition} from "../chart-tile/chart-tile.component";

@Component({
    selector: 'chart-type-selector',
    templateUrl: './chart-type-selector.component.html',
    styleUrls: ['./chart-type-selector.component.css']
})
export class ChartTypeSelectorComponent {
    @Input() availableChartDefinitions: ChartDefinition[] = []
    @Output() selectedChartDefinitionChange = new EventEmitter<ChartDefinition>()

    private _selectedChartDefinition!: ChartDefinition
    get selectedChartDefinition(): ChartDefinition {
        return this._selectedChartDefinition
    }

    @Input() set selectedChartDefinition(d: ChartDefinition) {
        this._selectedChartDefinition = d
        this.selectedChartDefinitionChange.emit(d)
    }

    // availableChartTypes: Record<string, Type<any>> = {
    //     "Lufttemperatur Min/Avg/Max": AirTemperatureChartComponent,
    //     "Luftfeuchtigkeit Min/Avg/Max": HumidityChartComponent,
    //     "Taupunkt Min/Avg/Max": DewPointTemperatureChartComponent,
    //     "Luftdruck Min/Avg/Max": AirPressureChartComponent,
    //     "Sichtweite Min/Avg/Max": VisibilityChartComponent,
    //     "Sonnenscheindauer": SunshineDurationChartComponent,
    //     "Lufttemperatur Details": AirTemperatureHeatmapChartComponent,
    //     "Sonnenschein Details": SunshineDurationHeatmapChartComponent,
    //     "Niederschlag": PrecipitationChartComponent
    // }

    // chartTypeChanged(event: Event) {
    //     let select = event.target as HTMLSelectElement
    //     let chartType = this.availableChartTypes[select.value]
    //     this.chartTypeSelected.emit(chartType)
    // }
}
