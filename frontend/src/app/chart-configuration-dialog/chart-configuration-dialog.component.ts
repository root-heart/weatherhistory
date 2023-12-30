import {ChangeDetectorRef, Component, ElementRef, EventEmitter, Output, ViewChild} from '@angular/core';
import {WeatherStationMap} from "../weather-station-map/weather-station-map.component";
import {WeatherStation} from "../WeatherStationService";

export const airTemperature = 'Lufttemperatur';
export const airPressure = 'Luftdruck';
export const humidity = 'Luftfeuchtigkeit';
export const dewPoint = 'Taupunkttemperatur';
export const sunshine = 'Sonnenschein';
export const windDirection = 'Windrichtung';
export const windSpeed = 'Windgeschwindigkeit';
export const cloudCoverage = 'Bedeckungsgrad';
export const cloudBase = 'Wolkenuntergrenze (WIP)';
export const rain = 'Regen';
export const snow = 'Schnee';
export const visibility = 'Sichtweite';

const MeasurementNames = [
    airTemperature,
    airPressure,
    humidity,
    dewPoint,
    sunshine,
    windDirection,
    windSpeed,
    cloudCoverage,
    cloudBase,
    rain,
    snow,
    visibility
] as const

export type MeasurementName = typeof MeasurementNames[number]

export type ChartConfiguration = {
    station: WeatherStation
    measurementName: MeasurementName
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

    selectedStation?: WeatherStation
    measurementName: MeasurementName = airTemperature
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
            this.confirmed.emit({station: this.selectedStation, year: this.year, measurementName: this.measurementName})
        }
    }

    closeDialogCancel() {
        this.dialog.nativeElement.close()
    }
}
