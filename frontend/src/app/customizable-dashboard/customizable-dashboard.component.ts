import {Component, ViewChildren} from '@angular/core';
import {ChartTileComponent} from "./chart-tile/chart-tile.component";

@Component({
  selector: 'customizable-dashboard',
  templateUrl: './customizable-dashboard.component.html',
  styleUrls: ['./customizable-dashboard.component.css']
})
export class CustomizableDashboardComponent {
    @ViewChildren(ChartTileComponent) chartTiles!: ChartTileComponent[]
}
