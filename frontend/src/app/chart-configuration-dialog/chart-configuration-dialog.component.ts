import {ChangeDetectorRef, Component, ElementRef, EventEmitter, Output, ViewChild} from '@angular/core';
import {WeatherStationMap} from "../weather-station-map/weather-station-map.component";
import {WeatherStation} from "../WeatherStationService";
import {ConfigurationWizardComponent} from "../configuration-wizard/configuration-wizard.component";

export class ChartType {
    constructor(public name: string, public iconName: string) {
    }

    // static daily = new ChartType("täglich", "daily")
    // static monthly = new ChartType("monatlich", "monthly")
    static overview = new ChartType("Übersicht", "monthly")
    static histogram = new ChartType("Histogramm", "histogram")
    static details = new ChartType("Heatmap", "heatmap")
}

export class Measurement {
    static airTemperature = new Measurement("Lufttemperatur", "thermometer", [ChartType.overview, ChartType.details, ChartType.histogram])
    static airPressure = new Measurement("Luftdruck", "barometer", [ChartType.overview, ChartType.histogram])
    static humidity = new Measurement("Luftfeuchtigkeit", "humidity", [ChartType.overview, ChartType.details, ChartType.histogram])
    static dewPoint = new Measurement("Taupunkt", "dew", [ChartType.overview, ChartType.details, ChartType.histogram])
    static sunshine = new Measurement("Sonnenschein", "sun", [ChartType.overview, ChartType.details, ChartType.histogram])
    static windDirection = new Measurement("Windrichtung", "compass", [])
    static windSpeed = new Measurement("Windgeschwindigkeit", "wind", [])
    static cloudCoverage = new Measurement("Wolkenbedeckung", "clouds", [ChartType.details, ChartType.histogram])
    // static cloudBase = new Measurement("Wolkenuntergrenze (WIP)", [])
    static rain = new Measurement("Regen", "rainy", [ChartType.overview, ChartType.histogram])
    static snow = new Measurement("Schnee", "snow", [ChartType.overview, ChartType.histogram])
    static visibility = new Measurement("Sichtweite", "foggy-night", [ChartType.overview, ChartType.details, ChartType.histogram])

    constructor(public name: string, public iconName: string, public possibleChartType: ChartType[]) {
    }
}

export const measurements = [
    Measurement.airTemperature,
    Measurement.rain,
    Measurement.sunshine,
    Measurement.windSpeed,
    Measurement.windDirection,
    Measurement.cloudCoverage,
    Measurement.airPressure,
    Measurement.humidity,
    Measurement.dewPoint,
    Measurement.snow,
    Measurement.visibility,
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
    @ViewChild(ConfigurationWizardComponent) configurationWizard?: ConfigurationWizardComponent

    @Output() confirmed = new EventEmitter<ChartConfiguration>()

    availableMeasurements = measurements
    allChartTypes = [ChartType.overview, ChartType.details, ChartType.histogram]

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

    get title(): string {
        return `Diagramm-Konfiguration - ${this.configurationWizard?.currentStep?.caption}`
    }

    selectedStation?: WeatherStation
    measurement?:  Measurement
    chartType?: ChartType
    year?: number

    minYear: number = new Date().getFullYear()
    maxYear: number = new Date().getFullYear()

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
        let firstMeasurementDate = new Date(Date.parse(station.firstMeasurementDateString))
        let lastMeasurementDate = new Date(Date.parse(station.lastMeasurementDateString))
        this.minYear = firstMeasurementDate.getFullYear()
        this.maxYear = lastMeasurementDate.getFullYear()
        this.configurationWizard!.currentStep!.complete = true
        this.changeDetector.detectChanges()
    }

    removeSelectedStation() {
        this.selectedStation = undefined
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
