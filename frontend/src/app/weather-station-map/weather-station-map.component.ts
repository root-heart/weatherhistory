import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {WeatherStation, WeatherStationList, WeatherStationService} from "../WeatherStationService";
import {
    Icon,
    LatLng, LatLngBounds,
    Layer,
    LayerGroup,
    LeafletMouseEvent,
    MapOptions,
    Marker,
    MarkerOptions,
    TileLayer,
    Map
} from "leaflet";
import {FilterService} from "../filter.service";
import {faMapLocationDot} from "@fortawesome/free-solid-svg-icons";

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
    selector: 'weather-station-map',
    templateUrl: './weather-station-map.component.html',
    styleUrls: ['./weather-station-map.component.css']
})
export class WeatherStationMap implements OnInit {
    @Input() selectedStation?: WeatherStation
    @Output() selectedStationChange = new EventEmitter<WeatherStation>()

    stations: WeatherStationList = []
    options: MapOptions = {
        layers: getLayers(),
        // zoom: 6,
        // center: new LatLng(51.4, 10.447683),
        zoomDelta: 0.1,
        zoomSnap: 0.1,
        maxBoundsViscosity: 0.9,
    };

    mapBounds = new LatLngBounds(new LatLng(47, 5.5), new LatLng(55.5, 15.5))
    stationsLayer?: Layer
    map?: Map

    constructor(private weatherStationService: WeatherStationService, public filterService: FilterService) {
        weatherStationService.getWeatherStations().subscribe(data => this.setStations(data));
    }

    ngOnInit(): void {
    }

    mapReady(map: Map) {
        this.map = map
    }

    invalidateSize() {
        this.map?.invalidateSize()
        this.map?.fitBounds(this.mapBounds)
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
        this.selectedStationChange.emit(mouseEvent.target.options.icon.options.station)
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
