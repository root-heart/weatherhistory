import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {SummaryJson} from "../SummaryService";
import {formatDate} from "@angular/common";

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
        'hsl(203, 70%, 70%)',
        'hsl(203, 70%, 85%)',
        'hsl(203, 35%, 85%)',
        'hsl(203, 0%, 85%)',
        'hsl(203, 0%, 75%)',
        'hsl(203, 0%, 65%)',
        'hsl(203, 0%, 55%)',
        'hsl(203, 0%, 45%)',
        'hsl(203, 0%, 35%)',
        'hsl(203, 0%, 25%)',

        'hsl(150, 50%, 30%)',
        'hsl(150, 50%, 20%)',
    ];

    private chartRight: number = 0
    private chartLeft: number = 0
    private chartTop: number = 0
    private chartBottom: number = 0
    private tileWidth: number = 0
    private tileHeight: number = 0

    constructor() {
    }

    ngOnInit(): void {
    }

    public setData(measurements: Array<SummaryJson>) {
        if (this.canvas === undefined) {
            return
        }
        let ctx = this.initializeCanvasContext()
        this.calculateChartArea(ctx, measurements)
        this.drawMeasurements(ctx, measurements)
        this.drawXScale(ctx, measurements)
        this.drawYScale(ctx)
    }

    private initializeCanvasContext(): CanvasRenderingContext2D {
        this.canvas!.nativeElement.width = this.canvas!.nativeElement.parentElement.clientWidth
        this.canvas!.nativeElement.height = this.canvas!.nativeElement.parentElement.clientHeight

        let ctx: CanvasRenderingContext2D = this.canvas!.nativeElement.getContext('2d')
        ctx.font = "1rem Arial"
        return ctx
    }

    private calculateChartArea(ctx: CanvasRenderingContext2D, measurements: Array<SummaryJson>) {
        let textMetrics: TextMetrics = ctx.measureText("22:00")
        this.chartLeft = textMetrics.width + 10
        this.chartRight = this.canvas!.nativeElement.clientWidth - 10
        let chartWidth = this.chartRight - this.chartLeft
        this.tileWidth = chartWidth / measurements.length

        textMetrics = ctx.measureText("August")
        this.chartTop = textMetrics.actualBoundingBoxAscent
        this.chartBottom = this.canvas!.nativeElement.clientHeight - (textMetrics.actualBoundingBoxAscent + textMetrics.actualBoundingBoxDescent + 7)
        let chartHeight = this.chartBottom - this.chartTop;
        this.tileHeight = chartHeight / 24
    }

    private drawMeasurements(ctx: CanvasRenderingContext2D, measurements: Array<SummaryJson>) {
        ctx.translate(this.chartLeft, this.chartBottom)
        measurements.forEach((summary, day) => {
            summary.coverages.forEach((coverage, hour) => {
                if (coverage !== undefined && coverage !== null && coverage >= 0) {
                    ctx.fillStyle = this.coverageColors[coverage]
                } else {
                    ctx.fillStyle = "hsl(0, 50%, 30%)"
                }
                ctx.fillRect(day * this.tileWidth, -hour * this.tileHeight, this.tileWidth + 1, -(this.tileHeight + 1))
            })
        })
        ctx.resetTransform()
    }

    private daysInMonth(month: number, year: number): number {
        return new Date(year, month, 0).getDate();
    }

    private getMonthNames(): string[] {
        return Array.from(Array(12).keys())
            .map(month => formatDate(new Date(2022, month, 1), "MMMM", "de-DE"))
    }

    private drawXScale(ctx: CanvasRenderingContext2D, measurements: Array<SummaryJson>) {

        ctx.translate(0, 0.5);
        ctx.beginPath()
        ctx.lineWidth = 1
        ctx.moveTo(this.chartLeft, this.chartBottom)
        ctx.lineTo(this.chartLeft, this.chartBottom)
        ctx.lineTo(this.chartRight, this.chartBottom)
        ctx.stroke()
        ctx.resetTransform()

        let year = measurements[0].firstDay.getFullYear()

        // draw month separator lines
        ctx.moveTo(0.5, 10)
        ctx.lineTo(0.5, this.chartTop - this.chartBottom + 10)
        ctx.stroke()

        ctx.textAlign = "center"
        ctx.textBaseline = "top"
        ctx.fillStyle = "#222"

        ctx.translate(this.chartLeft, this.chartBottom)
        this.getMonthNames().forEach((monthName, index) => {
            let dayCountPerMonth = this.daysInMonth(index + 1, year)
            console.log("Drawing " + monthName + " with " + dayCountPerMonth + " days")
            ctx.fillText(monthName, dayCountPerMonth * this.tileWidth / 2, 3)
            ctx.translate(dayCountPerMonth * this.tileWidth, 0)

        })
        ctx.resetTransform()

        this.getMonthNames().forEach((monthName, index) => {
            ctx.translate(this.chartLeft, this.chartTop)
            let dayCountPerMonth = 0
            Array.from(Array(index).keys()).forEach(v => {
                dayCountPerMonth += this.daysInMonth(v + 1, year)
            })
            console.log("Drawing month separator for " + monthName + " with " + dayCountPerMonth + " days")
            let x = Math.floor(dayCountPerMonth * this.tileWidth) + 0.5
            ctx.strokeStyle = "#222"
            ctx.translate(x, 0)
            ctx.beginPath()
            ctx.moveTo(0, 0)
            ctx.lineTo(0, this.chartBottom - this.chartTop)
            ctx.stroke()
            ctx.closePath()
            ctx.resetTransform()
        })
    }

    private drawYScale(ctx: CanvasRenderingContext2D) {
        ctx.translate(0.5, 0);
        ctx.beginPath()
        ctx.lineWidth = 1
        ctx.moveTo(this.chartLeft, this.chartTop)
        ctx.lineTo(this.chartLeft, this.chartBottom)
        ctx.stroke()
        ctx.resetTransform()

        ctx.fillStyle = "#222"

        ctx.translate(this.chartLeft - 10, this.chartBottom)
        ctx.textAlign = "end"
        ctx.textBaseline = "middle"
        ctx.fillText("0:00", 0, 0)
        ctx.fillText("3:00", 0, -this.tileHeight * 3)
        ctx.fillText("6:00", 0, -this.tileHeight * 6)
        ctx.fillText("9:00", 0, -this.tileHeight * 9)
        ctx.fillText("12:00", 0, -this.tileHeight * 12)
        ctx.fillText("15:00", 0, -this.tileHeight * 15)
        ctx.fillText("18:00", 0, -this.tileHeight * 18)
        ctx.fillText("21:00", 0, -this.tileHeight * 21)
        ctx.fillText("0:00", 0, -this.tileHeight * 24)
        ctx.resetTransform()
    }
}
