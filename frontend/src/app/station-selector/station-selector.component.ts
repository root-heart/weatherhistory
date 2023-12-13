import {ChangeDetectorRef, Component, EventEmitter, Input, Output} from '@angular/core';
import {faMapLocationDot} from "@fortawesome/free-solid-svg-icons";
import {WeatherStation} from "../WeatherStationService";

@Component({
  selector: 'station-selector',
  templateUrl: './station-selector.component.html',
  styleUrls: ['./station-selector.component.scss']
})
export class StationSelectorComponent {
    @Output() stationSelected = new EventEmitter<WeatherStation>()

    selectedStation?: WeatherStation
    selectedStationOnMap?: WeatherStation
    faMapLocationDot = faMapLocationDot
    mapVisible: boolean = false

    constructor( private changeDetector: ChangeDetectorRef) {

    }

    showMap() {
        this.mapVisible = true
    }

    selectedStationChangedOnMap(station: WeatherStation) {
        this.selectedStationOnMap = station
        this.changeDetector.detectChanges()
    }

    closeDialogOk() {
        this.selectedStation = this.selectedStationOnMap
        this.stationSelected.emit(this.selectedStationOnMap)
        this.mapVisible = false
    }

    closeDialogCancel() {
        this.selectedStationOnMap = this.selectedStation
        this.mapVisible = false
    }
}
