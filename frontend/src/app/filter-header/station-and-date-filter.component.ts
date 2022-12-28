import {ApplicationRef, Component, OnInit} from '@angular/core';
import {WeatherStation, WeatherStationList, WeatherStationService} from "../WeatherStationService";
import {currentData, currentFilter, DateRangeFilter, SummaryData} from "../SummaryData";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
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
import * as luxon from "luxon";

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
    DateRangeFilter = DateRangeFilter

    stations: WeatherStationList = []
    options: MapOptions = {
        layers: getLayers(),
        zoom: 6,
        center: new LatLng(51.4, 10.447683),
    };

    stationsLayer?: Layer

    constructor(private weatherStationService: WeatherStationService, private http: HttpClient, private app: ApplicationRef) {
        weatherStationService.getWeatherStations().subscribe(data => this.setStations(data));
    }

    ngOnInit(): void {
    }

    fireFilterChangedEvent(): void {
        if (currentFilter.selectedStation) {
            let stationId = currentFilter.selectedStation.id
            let url = ""
            if (currentFilter.dateRangeFilter === DateRangeFilter.THIS_MONTH) {
                let from = luxon.DateTime.now().startOf("month")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from.toFormat("yyyy/MM")}`
            } else if (currentFilter.dateRangeFilter === DateRangeFilter.LAST_MONTH) {
                let from = luxon.DateTime.now().minus({month: 1}).startOf("month")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from.toFormat("yyyy/MM")}`
            } else if (currentFilter.dateRangeFilter === DateRangeFilter.THIS_YEAR) {
                let from = luxon.DateTime.now().startOf("year")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from.toFormat("yyyy")}`
            } else if (currentFilter.dateRangeFilter === DateRangeFilter.LAST_YEAR) {
                let from = luxon.DateTime.now().minus({year: 1}).startOf("year")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from.toFormat("yyyy")}`
            }

            console.log(`fetching data from ${url}`)
            this.http
                .get<SummaryData>(url)
                .subscribe(data => {
                    // TODO i would rather like to put the data in some observable object and bind listeners to that
                    currentData.next(data)
                    // this.data = data
                    // this.onFilterChanged.emit(data)
                    // hmm, something in angular does not work, so i have to refresh everything on my own here...
                    this.app.tick()
                })
        }
    }

    filter(range: DateRangeFilter) {
        currentFilter.dateRangeFilter = range
        this.fireFilterChangedEvent()
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
        currentFilter.selectedStation = mouseEvent.target.options.icon.options.station
        this.fireFilterChangedEvent()
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
