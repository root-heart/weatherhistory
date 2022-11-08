import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, MeasurementDataSet} from "../BaseChart";
import {SummaryList} from "../SummaryService";

@Component({
    selector: 'precipitation-chart',
    template: '<canvas #precipitationChart></canvas>',
    styles: [':host {height: 25rem; display: block;}']
})
export class PrecipitationChart extends BaseChart implements OnInit {
    @ViewChild("precipitationChart")
    private canvas?: ElementRef;

    constructor() {
        super();
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: SummaryList): Array<MeasurementDataSet> {
        let dataSets: Array<MeasurementDataSet> = [];
        dataSets.push({
            type: 'bar',
            label: 'Regen',
            borderColor: 'hsl(240, 100%, 50%)',
            backgroundColor: 'hsl(240, 100%, 50%)',
            data: summaryList.map(m => m.sumRainfallMillimeters),
            categoryPercentage: 1,
            barPercentage: 1,
            showTooltip: true,
            showLegend: false,
            stack: 'bla'
            // tooltipValueFormatter: (value: number) => this.formatHours(value)
        });
        dataSets.push({
            type: 'bar',
            label: 'Schnee',
            borderColor: 'hsl(40, 0%, 80%)',
            backgroundColor: 'hsl(40, 0%, 80%)',
            data: summaryList.map(m => -m.sumSnowfallMillimeters),
            categoryPercentage: 1,
            barPercentage: 1,
            showTooltip: true,
            showLegend: false,
            stack: 'bla'
            // tooltipValueFormatter: (value: number) => this.formatHours(value)
        });
        return dataSets;
    }

}
