import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {SummaryJson} from "../summary/SummaryService";
import {formatDate} from "@angular/common";

@Component({
    selector: 'meteogram',
    template: '<div style="height: 70vw"><canvas style="background-color: #333" #meteogram></canvas>\</div>'
})
export class Meteogram implements OnInit {
    @ViewChild("meteogram")
    private canvas?: ElementRef

    private chartRight: number = NaN
    private chartLeft: number = NaN
    private chartTop: number = NaN
    private chartBottom: number = NaN
    private chartWidth: number = NaN
    private chartHeight: number = NaN

    private minDate?: Date = undefined
    private maxDate?: Date = undefined
    private dx: number = NaN

    private minTemperature: number = NaN
    private maxTemperature: number = NaN
    private temperatureChartZero: number = NaN
    private temperatureChartHeight: number = NaN
    private temperatureChartBottom: number = NaN
    private temperatureYMultiplier: number = NaN

    private precipitationChartZero: number = NaN

    private sortedMeasurements: Array<SummaryJson> = []

    constructor() {
    }

    ngOnInit(): void {
    }

    public setData(measurements: Array<SummaryJson>) {
        if (this.canvas === undefined) {
            return
        }
        this.sortedMeasurements = measurements.sort((m1, m2) => m1.firstDay.valueOf() - m2.firstDay.valueOf())

        let ctx = this.initializeCanvasContext()
        this.calculateChartArea(ctx)
        this.calculateTicks()
        this.drawPrecipitation(ctx)
        this.drawTemperatures(ctx)
        this.drawDewPointTemperature(ctx)
        this.drawCloudiness(ctx)
        // this.drawXScale(ctx, measurements)
        // this.drawYScale(ctx)
    }

    private initializeCanvasContext(): CanvasRenderingContext2D {
        this.canvas!.nativeElement.width = this.canvas!.nativeElement.parentElement.clientWidth
        this.canvas!.nativeElement.height = this.canvas!.nativeElement.parentElement.clientHeight

        let ctx: CanvasRenderingContext2D = this.canvas!.nativeElement.getContext('2d')
        ctx.font = "1rem Arial"
        return ctx
    }

    private calculateChartArea(ctx: CanvasRenderingContext2D) {
        let textMetrics: TextMetrics = ctx.measureText("22:00")
        this.chartLeft = textMetrics.width + 10
        this.chartRight = this.canvas!.nativeElement.clientWidth - 10
        this.chartWidth = this.chartRight - this.chartLeft

        textMetrics = ctx.measureText("August")
        this.chartTop = textMetrics.actualBoundingBoxAscent
        this.chartBottom = this.canvas!.nativeElement.clientHeight - (textMetrics.actualBoundingBoxAscent + textMetrics.actualBoundingBoxDescent + 7)
        this.chartHeight = this.chartBottom - this.chartTop

        console.log("Chart area: (" + this.chartLeft + "," + this.chartTop + ") - (" + this.chartRight + "," + this.chartBottom + ")")

        this.temperatureChartHeight = this.chartHeight * 0.3
        this.temperatureChartBottom = Math.floor(this.chartTop + this.temperatureChartHeight)
        console.log("temperature chart bottom = " + (this.chartTop + this.temperatureChartHeight))

        this.drawWhiteLine(ctx, this.temperatureChartBottom + 0.5)

        ctx.rect(this.chartLeft, this.chartTop, this.chartWidth, this.chartHeight)
        ctx.strokeStyle = "#fff"
        ctx.lineWidth = 1
        ctx.stroke()
    }

    private calculateTicks() {
        this.minDate = this.sortedMeasurements[0].firstDay
        this.maxDate = this.sortedMeasurements[this.sortedMeasurements.length - 1].firstDay
        this.dx = this.chartWidth / (this.maxDate.valueOf() - this.minDate.valueOf())
    }

    private calculateX(date: Date): number {
        return this.dx * (date.valueOf() - this.minDate!.valueOf())
    }

    private calculateTemperatureY(temperature: number): number {
        return -this.temperatureYMultiplier * temperature
    }

    private drawWhiteLine(ctx: CanvasRenderingContext2D, y: number) {
        ctx.beginPath()
        ctx.moveTo(this.chartLeft, y)
        ctx.lineTo(this.chartRight, y)
        ctx.strokeStyle = "#fff"
        ctx.lineWidth = 1
        ctx.stroke()
    }

    private drawTemperatures(ctx: CanvasRenderingContext2D) {
        let minAirTemperatures = this.sortedMeasurements.map(m => m.minDewPointTemperatureCentigrade).filter(v => v)
        this.minTemperature = Math.min.apply(null, minAirTemperatures)
        this.minTemperature = Math.floor(this.minTemperature / 5) * 5

        let maxAirTemperatures = this.sortedMeasurements.map(m => m.maxAirTemperatureCentigrade).filter(v => v);
        this.maxTemperature = Math.max.apply(null, maxAirTemperatures)
        this.maxTemperature = Math.ceil(this.maxTemperature / 5) * 5

        let temperatureRange = this.maxTemperature - this.minTemperature
        this.temperatureYMultiplier = this.temperatureChartHeight / temperatureRange
        this.temperatureChartZero = this.chartTop + this.temperatureYMultiplier * this.maxTemperature

        this.drawWhiteLine(ctx, this.temperatureChartZero)

        ctx.strokeStyle = 'rgba(200, 0, 0, 1)'
        ctx.lineWidth = 2
        ctx.lineCap = "round"
        ctx.lineJoin = "round"

        ctx.translate(this.chartLeft, this.temperatureChartZero);
        ctx.beginPath()
        ctx.moveTo(0, this.calculateTemperatureY(this.sortedMeasurements[0].avgAirTemperatureCentigrade))
        for (let i = 0; i < this.sortedMeasurements.length; i++) {
            let measurement = this.sortedMeasurements[i];
            ctx.lineTo(this.calculateX(measurement.firstDay), this.calculateTemperatureY(measurement.avgAirTemperatureCentigrade))
        }
        ctx.stroke()
        ctx.closePath()

        ctx.strokeStyle = 'rgba(200, 0,0, 50%)'
        ctx.fillStyle = 'rgba(200, 0,0, 20%)'
        ctx.lineWidth = 1

        let path = new Path2D()
        let minAirTemps = this.sortedMeasurements.map(m => new MeasurementPoint(m.firstDay, m.minAirTemperatureCentigrade));
        this.addToPath(path, minAirTemps)

        this.sortedMeasurements.reverse()
        path.lineTo(this.calculateX(this.sortedMeasurements[0].firstDay), this.calculateTemperatureY(this.sortedMeasurements[0].maxAirTemperatureCentigrade))
        let maxAirTempsReversed = this.sortedMeasurements.map(m => new MeasurementPoint(m.firstDay, m.maxAirTemperatureCentigrade));
        this.addToPath(path, maxAirTempsReversed)
        this.sortedMeasurements.reverse()

        ctx.fill(path)

        ctx.resetTransform()
    }


    private drawDewPointTemperature(ctx: CanvasRenderingContext2D) {
        ctx.strokeStyle = 'rgba(0, 200, 250, 1)'
        ctx.lineWidth = 1
        // ctx.setLineDash([2, 2])
        ctx.lineCap = "round"
        ctx.lineJoin = "round"

        ctx.translate(this.chartLeft, this.temperatureChartZero);
        ctx.beginPath()
        ctx.moveTo(0, this.calculateTemperatureY(this.sortedMeasurements[0].avgDewPointTemperatureCentigrade))
        for (let i = 0; i < this.sortedMeasurements.length; i++) {
            let measurement = this.sortedMeasurements[i];
            ctx.lineTo(this.calculateX(measurement.firstDay), this.calculateTemperatureY(measurement.avgDewPointTemperatureCentigrade))
        }
        ctx.stroke()

        ctx.beginPath()
        ctx.strokeStyle = 'rgba(0, 200, 250, 50%)'
        ctx.fillStyle = 'rgba(0, 200, 250, 10%)'
        ctx.lineWidth = 1

        let path = new Path2D()
        let minTemps = this.sortedMeasurements.map(m => new MeasurementPoint(m.firstDay, m.minDewPointTemperatureCentigrade));
        this.addToPath(path, minTemps)

        this.sortedMeasurements.reverse()
        path.lineTo(this.calculateX(this.sortedMeasurements[0].firstDay), this.calculateTemperatureY(this.sortedMeasurements[0].maxDewPointTemperatureCentigrade))
        let maxTempsReversed = this.sortedMeasurements.map(m => new MeasurementPoint(m.firstDay, m.maxDewPointTemperatureCentigrade));
        this.addToPath(path, maxTempsReversed)
        this.sortedMeasurements.reverse()

        ctx.fill(path)

        ctx.resetTransform()
    }

    private drawPrecipitation(ctx: CanvasRenderingContext2D) {
        ctx.translate(this.chartLeft, this.temperatureChartBottom)

        ctx.beginPath()
        for (let measurement of this.sortedMeasurements) {
            let x = this.calculateX(measurement.firstDay)
            let w = this.calculateX(measurement.lastDay) - x
            ctx.fillStyle = '#eee'
            ctx.fillRect(x, 0, w, -measurement.sumSnowfallMillimeters * 10)

            let y = measurement.sumSnowfallMillimeters ? -measurement.sumSnowfallMillimeters * 10 : 0
            ctx.fillStyle = '#00e'
            ctx.fillRect(x, y, w, -measurement.sumRainfallMillimeters * 10)
        }
        ctx.fill()
        ctx.resetTransform()
    }

    private drawCloudiness(ctx: CanvasRenderingContext2D) {

    }

    // private drawMeasurements(ctx: CanvasRenderingContext2D, measurements: Array<SummaryJson>) {
    //     ctx.translate(this.chartLeft, this.chartBottom)
    //     measurements.forEach((summary, day) => {
    //         summary.coverages.forEach((coverage, hour) => {
    //             if (coverage !== undefined && coverage !== null && coverage >= 0) {
    //                 ctx.fillStyle = this.coverageColors[coverage]
    //             } else {
    //                 ctx.fillStyle = "hsl(0, 50%, 30%)"
    //             }
    //             ctx.fillRect(day * this.tickWidth, -hour * this.tickHeight, this.tickWidth + 1, -(this.tickHeight + 1))
    //         })
    //     })
    //     ctx.resetTransform()
    // }

    private addToPath(path: Path2D, measurements: Array<MeasurementPoint>) {
        path.moveTo(0, -measurements[0] * 10)
        for (let i = 0; i < measurements.length; i++) {
            let measurement = measurements[i]
            path.lineTo(this.calculateX(measurement.measurementTime), this.calculateTemperatureY(measurement.value))
        }
    }

    private static daysInMonth(month: number, year: number): number {
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
            let dayCountPerMonth = Meteogram.daysInMonth(index + 1, year)
            console.log("Drawing " + monthName + " with " + dayCountPerMonth + " days")
            ctx.fillText(monthName, dayCountPerMonth * 45 /* TODO calculate x value correctly */ / 2, 3)
            ctx.translate(dayCountPerMonth * 45 /* TODO calculate x value correctly */, 0)

        })
        ctx.resetTransform()

        this.getMonthNames().forEach((monthName, index) => {
            ctx.translate(this.chartLeft, this.chartTop)
            let dayCountPerMonth = 0
            Array.from(Array(index).keys()).forEach(v => {
                dayCountPerMonth += Meteogram.daysInMonth(v + 1, year)
            })
            console.log("Drawing month separator for " + monthName + " with " + dayCountPerMonth + " days")
            let x = Math.floor(dayCountPerMonth * 45 /* TODO calculate x value correctly */) + 0.5
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
        // ctx.fillText("3:00", 0, -this.tickHeight * 3)
        // ctx.fillText("6:00", 0, -this.tickHeight * 6)
        // ctx.fillText("9:00", 0, -this.tickHeight * 9)
        // ctx.fillText("12:00", 0, -this.tickHeight * 12)
        // ctx.fillText("15:00", 0, -this.tickHeight * 15)
        // ctx.fillText("18:00", 0, -this.tickHeight * 18)
        // ctx.fillText("21:00", 0, -this.tickHeight * 21)
        // ctx.fillText("0:00", 0, -this.tickHeight * 24)
        ctx.resetTransform()
    }

}

class MeasurementPoint {
    constructor(public measurementTime: Date, public value: number) {

    }

}

