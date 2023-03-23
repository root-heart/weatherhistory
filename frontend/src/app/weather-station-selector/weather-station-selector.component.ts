import {Component, OnInit} from '@angular/core';
import {FilterService} from "../filter.service";

@Component({
    selector: 'weather-station-selector',
    templateUrl: './weather-station-selector.component.html',
    styleUrls: ['./weather-station-selector.component.css']
})
export class WeatherStationSelectorComponent implements OnInit {

    get position() {
        let selectedStation = this.filterService.selectedStation;
        if (selectedStation) {
            return selectedStation.latitude + "°N  / " + selectedStation?.longitude + "°O"
        }
        return ""
    }

    get height() {
        let selectedStation = this.filterService.selectedStation;
        if (selectedStation) {
            return selectedStation.height + "m"
        }
        return ""
    }

    constructor(public filterService: FilterService) {
    }

    ngOnInit(): void {
    }

}
