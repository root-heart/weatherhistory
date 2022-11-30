import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, BaseRecord, MeasurementDataSet} from "../BaseChart";
import {ScriptableContext} from "chart.js";

export type CloudinessRecord = BaseRecord & {
    hourlyCloudCoverage: number[]
}
@Component({
    selector: 'cloudiness-chart',
    template: '<canvas #cloudinessChart></canvas>',
    styleUrls: ['../charts.css']
})
export class CloudinessChart extends BaseChart<CloudinessRecord> implements OnInit {

    @ViewChild("cloudinessChart")
    private canvas?: ElementRef;

    private readonly coverageNames: Array<string> = [
        'wolkenlos',
        'vereinzelte Wolken',
        'heiter',
        'leicht bewölkt',
        'wolkig',
        'bewölkt',
        'stark bewölkt',
        'fast bedeckt',
        'bedeckt',
        'nicht erkennbar',
    ];

    private readonly coverageColors = [
        'hsl(210, 80%, 80%)',
        'hsl(210, 90%, 95%)',
        'hsl(55, 80%, 90%)',
        'hsl(55, 65%, 80%)',
        'hsl(55, 45%, 70%)',
        'hsl(55, 25%, 70%)',
        'hsl(55, 5%, 65%)',
        'hsl(55, 5%, 55%)',
        'hsl(55, 5%, 45%)',
        'hsl(55, 5%, 35%)',

        'hsl(0, 50%, 30%)',
        'hsl(0, 50%, 20%)',
    ];

    constructor() {
        super();
    }

    ngOnInit(): void {
    }

    protected getCanvas(): ElementRef | undefined {
        return this.canvas;
    }

    protected getDataSets(summaryList: Array<CloudinessRecord>): Array<MeasurementDataSet> {
        let coverageColors = this.coverageColors
        function getColor(context: ScriptableContext<'bar'>) {
            let hour = context.datasetIndex
            let data = summaryList[context.dataIndex]
            let coverage = data.hourlyCloudCoverage[hour]
            return coverage === undefined || coverage === null ? 'hsl(0, 50%, 30%)' : coverageColors[coverage];
        }

        let dataSets: Array<MeasurementDataSet> = [];
        for (let hour = 0; hour < 24; hour++) {
            dataSets.push({
                type: 'bar',
                label: 'Bewölkung Stunde ' + hour,
                borderColor: getColor,
                backgroundColor: getColor,
                data: new Array(summaryList.length).fill(1),
                showTooltip: true,
                showLegend: false,
                stack: 'bla',
                categoryPercentage: 1,
                barPercentage: 1,
            });
        }
        return dataSets;
    }

    protected getMaxY(): number | undefined {
        return 24
    }
}

