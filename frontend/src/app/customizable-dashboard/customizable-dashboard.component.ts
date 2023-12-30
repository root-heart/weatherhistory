import {Component, HostListener, ViewChild, ViewChildren} from '@angular/core';
import {ChartTileComponent} from "./chart-tile/chart-tile.component";
import {KtdGridComponent, KtdGridLayout, ktdTrackById} from "@katoid/angular-grid-layout";

@Component({
  selector: 'customizable-dashboard',
  templateUrl: './customizable-dashboard.component.html',
  styleUrls: ['./customizable-dashboard.component.css']
})
export class CustomizableDashboardComponent {
    @ViewChild(KtdGridComponent, {static: true}) grid?: KtdGridComponent
    @ViewChildren(ChartTileComponent) chartTiles?: ChartTileComponent[]

    gridRowHeight = 100
    gridColumns = 12
    layout: KtdGridLayout = [{id: '0', x: 0, y: 0, w: 6, h: 4},]
    trackById = ktdTrackById

    @HostListener('window:resize', ['$event'])
    onResize() {
        this.grid?.resize()
    }

    addChart() {
        const maxId = this.layout.reduce((acc, cur) => Math.max(acc, parseInt(cur.id, 10)), -1);
        const nextId = maxId + 1;

        // Important: Don't mutate the array, create new instance. This way notifies the Grid component that the layout has changed.
        this.layout = [
            ...this.layout,
            {id: nextId.toString(), x: 0, y: 0, w: 6, h: 4},
        ];
    }
}


