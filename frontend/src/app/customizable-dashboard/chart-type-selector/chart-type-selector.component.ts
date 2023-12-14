import {Component, ContentChild, EventEmitter, Input, Output, Type, ViewChild} from '@angular/core';
import {ChartDefinition} from "../chart-tile/chart-tile.component";
import {Dropdown} from "../../dropdown/dropdown.component";

@Component({
    selector: 'chart-type-selector',
    templateUrl: './chart-type-selector.component.html',
    styleUrls: ['./chart-type-selector.component.scss']
})
export class ChartTypeSelectorComponent {
    @Input() availableChartDefinitions: ChartDefinition[] = []
    @Output() selectedChartDefinitionChange = new EventEmitter<ChartDefinition>()

    @ViewChild(Dropdown)
    private dropdownComponent!: Dropdown

    private _selectedChartDefinition!: ChartDefinition
    get selectedChartDefinition(): ChartDefinition {
        return this._selectedChartDefinition
    }

    @Input() set selectedChartDefinition(chartDefinition: ChartDefinition) {
        if (this._selectedChartDefinition != chartDefinition) {
            this._selectedChartDefinition = chartDefinition
            console.log(`emit chart definition change event ${chartDefinition.name}`)
            this.selectedChartDefinitionChange.emit(chartDefinition)
        }
    }

    selectDefinitionAndCloseDropdown(chartDefinition: ChartDefinition) {
        this.selectedChartDefinition = chartDefinition
        this.dropdownComponent.dropdownVisible = false
    }
}
