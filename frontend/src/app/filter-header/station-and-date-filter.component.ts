import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormControl} from "@angular/forms";
import {WeatherStation, WeatherStationList, WeatherStationService} from "../WeatherStationService";

export class FilterChangedEvent {
    station: WeatherStation
    start: number
    end: number

    constructor(station: WeatherStation, start: number, end: number) {
        this.station = station;
        this.start = start;
        this.end = end;
    }
}

@Component({
    selector: 'station-and-date-filter',
    templateUrl: './station-and-date-filter.component.html',
    styleUrls: ['./station-and-date-filter.component.css']
})
export class StationAndDateFilterComponent implements OnInit {
    filterInput = new FormControl("")
    fromYear = new FormControl(2008)
    toYear = new FormControl(2022)

    stations: WeatherStationList = []
    selectedStation?: WeatherStation

    @Output() onFilterChanged = new EventEmitter<FilterChangedEvent>()

    constructor(private weatherStationService: WeatherStationService) {
        weatherStationService.getWeatherStations().subscribe(data => this.setStations(data));
    }

    private setStations(stations: Array<WeatherStation>) {
        console.log(stations)
        stations.sort((a, b) => a.name.localeCompare(b.name))
        this.stations = stations
    }

    ngOnInit(): void {
    }

    fireFilterChangedEvent(): void {
        // let station: WeatherStation = this.weatherStationFilterInput.value
        console.log("filter changed? " + this.selectedStation?.name + " - " + this.fromYear.value + " - " + this.toYear.value)
        if (this.selectedStation && this.fromYear.value && this.toYear.value) {
            console.log("filter changed!")
            this.onFilterChanged.emit({
                station: this.selectedStation,
                start: this.fromYear.value,
                end: this.toYear.value
            })
        }
    }

    log(message: String) {
        console.log(message)
    }
}
