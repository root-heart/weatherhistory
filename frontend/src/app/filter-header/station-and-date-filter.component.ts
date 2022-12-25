import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormControl} from "@angular/forms";
import {WeatherStation, WeatherStationList, WeatherStationService} from "../WeatherStationService";
import {SummaryData} from "../SummaryData";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";

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
    fromYear = new FormControl(2017)
    toYear = new FormControl(2022)

    stations: WeatherStationList = []
    selectedStation?: WeatherStation

    @Output() onFilterChanged = new EventEmitter<SummaryData>()
    data?: SummaryData;

    constructor(private weatherStationService: WeatherStationService, private http: HttpClient) {
        weatherStationService.getWeatherStations().subscribe(data => this.setStations(data));
    }

    private setStations(stations: Array<WeatherStation>) {
        // console.log(stations)
        stations.sort((a, b) => a.name.localeCompare(b.name))
        this.stations = stations
    }

    ngOnInit(): void {
    }

    fireFilterChangedEvent(): void {
        // let station: WeatherStation = this.weatherStationFilterInput.value
        // console.log("filter changed? " + this.selectedStation?.name + " - " + this.fromYear.value + " - " + this.toYear.value)
        if (this.selectedStation?.id && this.fromYear.value && this.toYear.value) {
            let url = `${environment.apiServer}/stations/${this.selectedStation?.id}/summary/${this.fromYear.value}`
            this.http
                .get<SummaryData>(url)
                .subscribe(data => {
                    this.data = data
                    this.onFilterChanged.emit(data)
                })
        }
    }

    // log(message: String) {
    //     console.log(message)
    // }
}
