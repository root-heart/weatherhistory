import {Component, HostListener, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {ChartTileComponent} from "./chart-tile/chart-tile.component";
import {KtdGridComponent, KtdGridLayout, ktdTrackById} from "@katoid/angular-grid-layout";
import {
    ChartConfiguration,
    ChartConfigurationDialog
} from "../chart-configuration-dialog/chart-configuration-dialog.component";
import {WeatherStation} from "../WeatherStationService";

@Component({
  selector: 'customizable-dashboard',
  templateUrl: './customizable-dashboard.component.html',
  styleUrls: ['./customizable-dashboard.component.css']
})
export class CustomizableDashboardComponent {
    @ViewChild(KtdGridComponent, {static: true}) grid?: KtdGridComponent
    @ViewChildren(ChartTileComponent) chartTiles!: QueryList<ChartTileComponent>
    @ViewChild(ChartConfigurationDialog) chartConfigurationDialog!: ChartConfigurationDialog

    gridRowHeight = 100
    gridColumns = 12
    layout: KtdGridLayout = [{id: '0', x: 0, y: 0, w: 6, h: 4},]
    trackById = ktdTrackById

    private currentlyConfiguredChartTile?: ChartTileComponent;

    @HostListener('window:resize', ['$event'])
    onResize() {
        this.grid?.resize()
    }

    addChart() {
        const maxId = this.layout.reduce((acc, cur) => Math.max(acc, parseInt(cur.id, 10)), -1);
        const nextId = maxId + 1;
        this.layout = [
            ...this.layout,
            {id: nextId.toString(), x: 0, y: 0, w: 6, h: 4},
        ];
    }

    openChartConfigurationDialog(id: string) {
        let gridItemIndex = this.layout.findIndex(v => v.id === id)
        this.currentlyConfiguredChartTile = this.chartTiles.get(gridItemIndex)!
        this.chartConfigurationDialog.show(
            this.currentlyConfiguredChartTile.weatherStation,
            this.currentlyConfiguredChartTile.measurementName,
            this.currentlyConfiguredChartTile.year
            )
    }

    removeChart(id: string) {
        this.layout = this.layout.filter(v => v.id !== id)
    }

    chartConfigurationConfirmed(chartConfig: ChartConfiguration) {
        console.log(chartConfig)
        this.currentlyConfiguredChartTile!.updateChartComponent(chartConfig.station,
            chartConfig.measurementName, chartConfig.chartType, chartConfig.year)
    }
}


