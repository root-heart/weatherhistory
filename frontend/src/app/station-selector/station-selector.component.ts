import {ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {faMapLocationDot} from "@fortawesome/free-solid-svg-icons";
import {WeatherStation} from "../WeatherStationService";
import {WeatherStationMap} from "../weather-station-map/weather-station-map.component";

@Component({
  selector: 'station-selector',
  templateUrl: './station-selector.component.html',
  styleUrls: ['./station-selector.component.scss']
})
export class StationSelectorComponent {
    @ViewChild("dialog") dialog!: ElementRef<HTMLDialogElement>
    @ViewChild(WeatherStationMap) map!: WeatherStationMap

    @Output() stationSelected = new EventEmitter<WeatherStation>()

    selectedStation?: WeatherStation
    selectedStationOnMap?: WeatherStation
    faMapLocationDot = faMapLocationDot

    constructor( private changeDetector: ChangeDetectorRef) {

    }

    showMap() {
        this.dialog.nativeElement.showModal()
        setTimeout(() => this.map.invalidateSize(), 0)
    }

    selectedStationChangedOnMap(station: WeatherStation) {
        this.selectedStationOnMap = station
        this.changeDetector.detectChanges()
    }

    closeDialogOk() {
        this.dialog.nativeElement.close()
        this.selectedStation = this.selectedStationOnMap
        this.stationSelected.emit(this.selectedStationOnMap)
    }

    closeDialogCancel() {
        this.dialog.nativeElement.close()
        this.selectedStationOnMap = this.selectedStation
    }
}
