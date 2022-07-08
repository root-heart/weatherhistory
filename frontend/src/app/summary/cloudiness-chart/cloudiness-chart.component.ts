import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BaseChart, MeasurementDataSet} from "../BaseChart";
import {CloudCoverageJson, SummaryJson, SummaryList} from "../SummaryService";
import {
    BarController, BarElement,
    CategoryScale,
    Chart,
    Filler,
    Legend,
    LinearScale,
    LineController,
    LineElement,
    PointElement, ScatterController,
    Tooltip
} from "chart.js";

@Component({
    selector: 'cloudiness-chart',
    templateUrl: 'cloudiness-chart.component.html',
})
export class CloudinessChart implements OnInit {

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
        /*'#f00',*/ 'hsl(210, 80%, 80%)',
        /*'#0f0',*/ 'hsl(210, 90%, 95%)',
        /*'#00f',*/ 'hsl(55, 80%, 90%)',
        /*'#ff0',*/ 'hsl(55, 65%, 80%)',
        /*'#f0f',*/ 'hsl(55, 45%, 70%)',
        /*'#0ff',*/ 'hsl(55, 25%, 70%)',
        /*'#800',*/ 'hsl(55, 5%, 65%)',
        /*'#080',*/ 'hsl(55, 5%, 55%)',
        /*'#008',*/ 'hsl(55, 5%, 45%)',
        /*'#880',*/ 'hsl(55, 5%, 35%)',
    ];

    constructor() {
        // super();
        // Chart.register(ScatterController, BarController, BarElement, LineController, LineElement, PointElement, CategoryScale, LinearScale, Tooltip, Legend, Filler);
    }

    ngOnInit(): void {
    }

    // protected getDataSets(summaryList: SummaryList): MeasurementDataSet[] {
    //     console.log(summaryList)
    //     let dataSets: Array<MeasurementDataSet> = [];
    //     for (let i = 0; i <= 8; i++) {
    //         dataSets.push({
    //             type: "scatter",
    //             label: 'Bedeckung ' + i,
    //             borderColor: this.coverageColors[i],
    //             backgroundColor: this.coverageColors[i],
    //             data: [],
    //             showTooltip: true,
    //             showLegend: false,
    //             pointStyle: "line",
    //             pointRotation: 90,
    //             pointBorderWidth: 5,
    //             pointRadius: 10
    //         });
    //     }
    //     summaryList.forEach((summary, day) => {
    //         summary.coverages.forEach((coverage, hour) => {
    //             if (coverage !== undefined && coverage !== null && coverage >= 0) {
    //                 dataSets[coverage].data.push({x: day, y: hour})
    //             }
    //         })
    //     })
    //     console.log(dataSets)
    //     return dataSets;
    // }

    protected getCanvas(): ElementRef<any> | undefined {
        return this.canvas;
    }

    public setData(measurements: Array<SummaryJson>) {
        console.log("hier")
        if (this.canvas === undefined) {
            return
        }
        let ctx: CanvasRenderingContext2D = this.canvas.nativeElement.getContext('2d')

        this.canvas.nativeElement.width = this.canvas.nativeElement.parentElement.clientWidth
        this.canvas.nativeElement.height = this.canvas.nativeElement.parentElement.clientHeight

        let tileWidth = this.canvas.nativeElement.clientWidth / measurements.length
        let tileHeight = this.canvas.nativeElement.clientHeight / 24

        measurements.forEach((summary, day) => {
            summary.coverages.forEach((coverage, hour) => {
                if (coverage !== undefined && coverage !== null && coverage >= 0) {
                    ctx.fillStyle = this.coverageColors[coverage]
                } else {
                    ctx.fillStyle = "hsl(0, 60%, 30%)"
                }
                ctx.strokeStyle = "black"
                ctx.lineWidth = 1
                ctx.fillRect(day * tileWidth, hour * tileHeight, tileWidth, tileHeight)
            })
        })
        ctx.translate(0.5, 0.5);
        ctx.beginPath()
        ctx.lineWidth = 1
        ctx.moveTo(0, 20)
        ctx.lineTo(0, this.canvas.nativeElement.clientHeight - 20)
        ctx.lineTo(this.canvas.nativeElement.clientWidth - 20, this.canvas.nativeElement.clientHeight - 20)
        ctx.stroke()
    }
}
