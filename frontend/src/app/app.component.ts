import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {FilterService} from "./filter.service";
import {DropdownService} from "./dropdown.service";

export type MeasurementTypes = "temperature" | "humidity" | "airPressure" | "visibility"

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements AfterViewInit {
  title = 'wetterchroniken.de/';

  @ViewChild("dropdownBackground") dropdownBackground?: ElementRef

  constructor(public filterService: FilterService, private dropdownService: DropdownService) {
  }

  ngAfterViewInit() {
      // TODO remove
    this.dropdownService.dropdownBackground = this.dropdownBackground
    setTimeout(() => {
      // console.log("ngAfterViewInit")
      // console.log(this.qqq)
      // console.log(this.temperatureChart)
      //
      // this.temperatureChart?.updateChart()

    })
  }
}

