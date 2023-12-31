import {Component, HostListener, Inject, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {ChartTileComponent} from "./chart-tile/chart-tile.component";
import {KtdGridComponent, KtdGridLayout, ktdTrackById} from "@katoid/angular-grid-layout";
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'customizable-dashboard',
  templateUrl: './customizable-dashboard.component.html',
  styleUrls: ['./customizable-dashboard.component.css']
})
export class CustomizableDashboardComponent {
    @ViewChild(KtdGridComponent, {static: true}) grid?: KtdGridComponent
    @ViewChildren(ChartTileComponent) chartTiles!: QueryList<ChartTileComponent>

    gridRowHeight = 50
    gridColumns = 12
    layout: KtdGridLayout = [{id: '0', x: 0, y: 0, w: 6, h: 8},]
    trackById = ktdTrackById

    constructor( @Inject(DOCUMENT) public document: Document) {
    }

    @HostListener('window:resize', ['$event'])
    onResize() {
        console.log("resize event handler")
        this.grid?.resize()
        this.chartTiles.forEach(t => t.reflowChart())
    }

    addChart() {
        const maxId = this.layout.reduce((acc, cur) => Math.max(acc, parseInt(cur.id, 10)), -1);
        const nextId = maxId + 1;
        this.layout = [
            ...this.layout,
            {id: nextId.toString(), x: 0, y: 0, w: 6, h: 4},
        ];
    }

    removeChart(id: string) {
        console.log(this)
        this.layout = this.layout.filter(v => v.id !== id)
    }

    protected readonly Document = Document;
}


