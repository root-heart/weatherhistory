import {ChangeDetectorRef, Component, ElementRef, EventEmitter, Output, ViewChild} from '@angular/core';
import {WeatherStationMap} from "../weather-station-map/weather-station-map.component";
import {WeatherStation} from "../WeatherStationService";

export class ChartType {
    constructor(public name: string) {
    }

    static daily = new ChartType("täglich")
    static monthly = new ChartType("monatlich")
    static histogram = new ChartType("Histogramm")
    static details = new ChartType("Detailliert")
}

export class Measurement {
    constructor(public name: string, public possibleChartType: ChartType[]) {
    }

    static airTemperature = new Measurement("Lufttemperatur", [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram])
    static airPressure = new Measurement("Luftdruck", [ChartType.monthly, ChartType.daily, ChartType.histogram])
    static humidity = new Measurement("Luftfeuchtigkeit", [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram])
    static dewPoint = new Measurement("Taupunkttemperatur", [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram])
    static sunshine = new Measurement("Sonnenschein", [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram])
    static windDirection = new Measurement("Windrichtung", [])
    static windSpeed = new Measurement("Windgeschwindigkeit", [])
    static cloudCoverage = new Measurement("Bedeckungsgrad", [ChartType.details, ChartType.histogram])
    static cloudBase = new Measurement("Wolkenuntergrenze (WIP)", [])
    static rain = new Measurement("Regen", [ChartType.monthly, ChartType.daily, ChartType.histogram])
    static snow = new Measurement("Schnee", [ChartType.monthly, ChartType.daily, ChartType.histogram])
    static visibility = new Measurement("Sichtweite", [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram])
}

export const chartTypes = [
    ChartType.monthly,
    ChartType.daily,
    ChartType.details,
    ChartType.histogram
] as const

export const measurements = [
    Measurement.airPressure,
    Measurement.airTemperature,
    Measurement.cloudBase,
    Measurement.cloudCoverage,
    Measurement.dewPoint,
    Measurement.humidity,
    Measurement.rain,
    Measurement.snow,
    Measurement.sunshine,
    Measurement.visibility,
    Measurement.windDirection,
    Measurement.windSpeed,
] as const

export type ChartConfiguration = {
    station?: WeatherStation
    measurement?: Measurement
    chartType?: ChartType
    year?: number
}

@Component({
  selector: 'chart-configuration-dialog',
  templateUrl: './chart-configuration-dialog.component.html',
  styleUrls: ['./chart-configuration-dialog.component.scss']
})
export class ChartConfigurationDialog {
    @ViewChild("dialog") dialog!: ElementRef<HTMLDialogElement>
    @ViewChild(WeatherStationMap) map!: WeatherStationMap

    @Output() confirmed = new EventEmitter<ChartConfiguration>()

    availableMeasurements = measurements

    get chartTypes(): ChartType[] {
        if (this.measurement === undefined) {
            return []
        }
        let chartTypes = this.measurement?.possibleChartType
        if (chartTypes === undefined) {
            return []
        }
        return chartTypes
    }

    selectedStation?: WeatherStation
    measurement?:  Measurement
    chartType?: ChartType
    year?: number

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    show(station: WeatherStation | undefined, measurementName: Measurement | undefined, year?: number) {
        this.selectedStation = station
        this.measurement = measurementName
        this.year = year
        this.dialog.nativeElement.showModal()
        setTimeout(() => this.map.invalidateSize(), 0)
    }

    selectedStationChangedOnMap(station: WeatherStation) {
        this.selectedStation = station
        this.changeDetector.detectChanges()
    }

    measurementChanged(m: Measurement) {
        this.measurement = m
    }

    chartTypeChanged(t: ChartType) {
        this.chartType = t
    }

    closeDialogOk() {
        if (this.selectedStation) {
            this.dialog.nativeElement.close()
            this.confirmed.emit({station: this.selectedStation, year: this.year, measurement: this.measurement, chartType: this.chartType})
        }
    }

    closeDialogCancel() {
        this.dialog.nativeElement.close()
    }
}
