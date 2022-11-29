import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord, MeasurementDataSet} from "../BaseChart";

export type PrecipitationRecord = BaseRecord & {
    sumRainfallMillimeters: number,
    sumSnowfallMillimeters: number
}

@Component({
    selector: 'precipitation-chart',
    template: '<canvas #precipitationChart></canvas>',
    styleUrls: ['../charts.css']
})
export class PrecipitationChart extends BaseChart<PrecipitationRecord> implements OnInit {
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

    protected getDataSets(summaryList: Array<PrecipitationRecord>): Array<MeasurementDataSet> {
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
            data: summaryList.map(m => {
                if (m.sumSnowfallMillimeters) {
                    return -m.sumSnowfallMillimeters
                } else {
                    return null
                }
            }),
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
