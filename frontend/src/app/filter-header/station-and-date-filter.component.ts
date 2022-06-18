import {Component, ElementRef, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
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
    fromYear = new FormControl(2008)
    toYear = new FormControl(2022)

    stations: WeatherStationList = []
    station: WeatherStation | null = null
    filteredStations?: WeatherStationList;

    @ViewChild("stationsFilterInput") stationsFilterInput?: ElementRef<HTMLInputElement>

    @Output() onFilterChanged = new EventEmitter<FilterChangedEvent>()
    stationsPopupVisible = false

    constructor(private weatherStationService: WeatherStationService) {
        weatherStationService.getWeatherStations().subscribe(data => this.setStations(data));
    }

    private setStations(stations: Array<WeatherStation>) {
        console.log(stations)
        this.stations = stations
    }

    ngOnInit(): void {
    }

    fireFilterChangedEvent(): void {
        // let station: WeatherStation = this.weatherStationFilterInput.value
        console.log("filter changed? " + this.station?.name + " - " + this.fromYear.value + " - " + this.toYear.value)
        if (this.station && this.fromYear.value && this.toYear.value) {
            console.log("filter changed!")
            this.onFilterChanged.emit({
                station: this.station,
                start: this.fromYear.value,
                end: this.toYear.value
            })
        }
    }

    filterStations(value: string) {
        const filterValue = value.toLowerCase();
        console.log("filter for " + filterValue)
        this.filteredStations = this.stations.filter(station => station.name.toLowerCase().includes(filterValue));
    }

    selectStation(station: WeatherStation) {
        console.log("select station " + station)
        this.station = station
        this.fireFilterChangedEvent()
    }

    log(message: String) {
        console.log(message)
    }
}
