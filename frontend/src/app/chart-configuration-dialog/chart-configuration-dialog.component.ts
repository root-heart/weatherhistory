import {ChangeDetectorRef, Component, ElementRef, EventEmitter, Output, ViewChild} from '@angular/core';
import {WeatherStationMap} from "../weather-station-map/weather-station-map.component";
import {WeatherStation} from "../WeatherStationService";

export enum MeasurementName {
    airTemperature = "Lufttemperatur",
    airPressure = "Luftdruck",
    humidity = "Luftfeuchtigkeit",
    dewPoint = "Taupunkttemperatur",
    sunshine = "Sonnenschein",
    windDirection = "Windrichtung",
    windSpeed = "Windgeschwindigkeit",
    cloudCoverage = "Bedeckungsgrad",
    cloudBase = "Wolkenuntergrenze (WIP)",
    rain = "Regen",
    snow = "Schnee",
    visibility = "Sichtweite"
}

export enum ChartType {
    daily = "daily",
    monthly = "monthly",
    histogram = "histogram",
    details = "details"
}

export const ChartTypes = [
    ChartType.monthly,
    ChartType.daily,
    ChartType.details,
    ChartType.histogram
] as const

export const MeasurementNames = [
    MeasurementName.airPressure,
    MeasurementName.airTemperature,
    MeasurementName.cloudBase,
    MeasurementName.cloudCoverage,
    MeasurementName.dewPoint,
    MeasurementName.humidity,
    MeasurementName.rain,
    MeasurementName.snow,
    MeasurementName.sunshine,
    MeasurementName.visibility,
    MeasurementName.windDirection,
    MeasurementName.windSpeed,
] as const

export type ChartConfiguration = {
    station: WeatherStation
    measurementName: MeasurementName
    chartType: ChartType
    year: number
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

    availableMeasurementNames = MeasurementNames


    measurementsAndChartTypes = new Map<MeasurementName, ChartType[]>([
        [MeasurementName.airTemperature, [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram]],
        [MeasurementName.airPressure, [ChartType.monthly, ChartType.daily, ChartType.histogram]],
        [MeasurementName.humidity, [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram]],
        [MeasurementName.dewPoint, [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram]],
        [MeasurementName.sunshine, [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram]],
        [MeasurementName.cloudCoverage, [ChartType.details, ChartType.histogram]],
        [MeasurementName.rain, [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram]],
        [MeasurementName.snow, [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram]],
        [MeasurementName.visibility, [ChartType.monthly, ChartType.daily, ChartType.details, ChartType.histogram]],
    ])

    selectedStation?: WeatherStation
    measurementName = MeasurementName.airTemperature
    chartType = ChartType.details
    year: number = 2023

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    show(station: WeatherStation | undefined, measurementName: MeasurementName, year: number) {
        this.selectedStation = station
        this.measurementName = measurementName
        this.year = year
        this.dialog.nativeElement.showModal()
        setTimeout(() => this.map.invalidateSize(), 0)
    }

    selectedStationChangedOnMap(station: WeatherStation) {
        this.selectedStation = station
        this.changeDetector.detectChanges()
    }

    closeDialogOk() {
        if (this.selectedStation) {
            this.dialog.nativeElement.close()
            this.confirmed.emit({station: this.selectedStation, year: this.year, measurementName: this.measurementName, chartType: this.chartType})
        }
    }

    closeDialogCancel() {
        this.dialog.nativeElement.close()
    }
}
