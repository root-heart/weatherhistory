import {Component, OnInit} from '@angular/core';
import {WeatherStation, WeatherStationList, WeatherStationService} from "../WeatherStationService";
import {currentFilter, DateRangeFilter} from "../SummaryData";
import {
    Icon,
    LatLng,
    Layer,
    LayerGroup,
    LeafletMouseEvent,
    MapOptions,
    Marker,
    MarkerOptions,
    TileLayer
} from "leaflet";
import {FilterService} from "../filter.service";

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
    selector: 'map-dropdown',
    templateUrl: './map-dropdown.component.html',
    styleUrls: ['./map-dropdown.component.css']
})
export class MapDropdown implements OnInit {
    stations: WeatherStationList = []
    options: MapOptions = {
        layers: getLayers(),
        zoom: 6,
        center: new LatLng(51.4, 10.447683),
    };

    stationsLayer?: Layer

    dropdownVisible: boolean = false

    constructor(private weatherStationService: WeatherStationService, public filterService: FilterService) {
        weatherStationService.getWeatherStations().subscribe(data => this.setStations(data));
    }

    ngOnInit(): void {
    }

    private setStations(stations: Array<WeatherStation>) {
        stations.sort((a, b) => a.name.localeCompare(b.name))
        this.stations = stations
        let markers = stations.map(s => this.stationToMarker(s));
        this.stationsLayer = new LayerGroup<any>(markers)
    }

    private stationToMarker(s: WeatherStation): Marker {
        let latLng = new LatLng(s.latitude, s.longitude);
        let options = {
            icon: new Icon({
                iconSize: [30, 30],
                iconAnchor: [15, 28],
                iconUrl: '/assets/map-pin.png',
                station: s
            }),
            title: s.name
        } as MarkerOptions;
        let marker = new Marker(latLng, options)
        marker.on("click", e => this.selectStation(e))
        return marker
    }

    private selectStation(mouseEvent: LeafletMouseEvent) {
        this.filterService.selectedStation = mouseEvent.target.options.icon.options.station
        this.filterService.fireFilterChangedEvent()
    }
}

export const getLayers = (): Layer[] => {
    let options = {
        attribution: '&copy; OpenStreetMap contributors'
    };
    return [
        new TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', options),
    ];
};
