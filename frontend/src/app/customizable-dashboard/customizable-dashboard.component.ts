import { Component } from '@angular/core';
import {ChartComponentBase} from "../charts/chart-component-base";
import {WindDirectionChart} from "../charts/wind-direction-chart/wind-direction-chart.component";

@Component({
  selector: 'customizable-dashboard',
  templateUrl: './customizable-dashboard.component.html',
  styleUrls: ['./customizable-dashboard.component.css']
})
export class CustomizableDashboardComponent {
    charts: ChartComponentBase[] = []


    testChart = WindDirectionChart
}
