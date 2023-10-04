import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'range-slider',
  templateUrl: './range-slider.component.html',
  styleUrls: ['./range-slider.component.css']
})
export class RangeSliderComponent implements OnInit {

    minYear: number = 1900
    maxYear: number = 2023
    upperYear: number = 2023
    lowerYear: number = 2000
    ticks: number[] = [1925, 1950, 1975, 2000]
  constructor() { }

  ngOnInit(): void {
  }

}
