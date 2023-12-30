import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {DateRangeSelector} from "./navigation/date-range-dropdown/date-range-selector.component";
import {FilterService} from "./filter.service";
import {YearSelectionComponent} from "./navigation/date-range-dropdown/year-selection/year-selection.component";
import {DropdownList} from './dropdown/dropdown-list/dropdown-list.component';
import {MonthSelectionComponent} from "./navigation/date-range-dropdown/month-selection/month-selection.component";
import {ToggableButtonComponent} from './toggable-button/toggable-button.component';

import {AppComponent} from './app.component';
import {RouterModule} from "@angular/router";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {WeatherStationMap} from "./weather-station-map/weather-station-map.component";
import {LeafletModule} from "@asymmetrik/ngx-leaflet";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from '@angular/common/http';
import {HighchartsChartModule} from "highcharts-angular";
import {MinAvgMaxChart} from './charts/min-avg-max-chart/min-avg-max-chart.component';
import {SumChartComponent} from './charts/sum-chart/sum-chart.component';
import {MatSliderModule} from "@angular/material/slider";
import {MatInputModule} from "@angular/material/input";
import {NgxSliderModule} from "ngx-slider-v2";
import {WindDirectionChart} from './charts/wind-direction-chart/wind-direction-chart.component';
import {HeatmapChart} from './charts/heatmap-chart/heatmap-chart.component';
import {CustomizableDashboardComponent} from './customizable-dashboard/customizable-dashboard.component';
import {ChartTileComponent} from './customizable-dashboard/chart-tile/chart-tile.component';
import {StationSelectorComponent} from './station-selector/station-selector.component';
import {AirTemperatureChartComponent} from './charts/measurement/air-temperature-chart.component';
import {
    DewPointTemperatureChartComponent
} from "./charts/measurement/dew-point-temperature-chart.component";
import { HumidityChartComponent } from './charts/measurement/humidity-chart.component';
import { AirPressureChartComponent } from './charts/measurement/air-pressure-chart.component';
import { VisibilityChartComponent } from './charts/measurement/visibility-chart.component';
import {SunshineDurationChartComponent} from "./charts/measurement/sunshine-duration-chart.component";
import { AirTemperatureHeatmapChartComponent } from './charts/measurement/air-temperature-heatmap-chart/air-temperature-heatmap-chart.component';
import { SunshineDurationHeatmapChartComponent } from './charts/measurement/sunshine-duration-heatmap-chart/sunshine-duration-heatmap-chart.component';
import { RainChartComponent } from './charts/measurement/rain-chart.component';
import { DropdownBoxComponent } from './dropdown/dropdown-box/dropdown-box.component';
import { CloudCoverageChartComponent } from './charts/measurement/cloud-coverage-chart/cloud-coverage-chart.component';
import { SunshineCloudCoverageHeatmapChartComponent } from './charts/measurement/sunshine-cloud-coverage-heatmap-chart/sunshine-cloud-coverage-heatmap-chart.component';
import {SnowChartComponent} from "./charts/measurement/snow-chart.component";
import {KtdGridModule} from "@katoid/angular-grid-layout";


@NgModule({
    declarations: [
        AppComponent,
        ToggableButtonComponent, DropdownList, DateRangeSelector,
        YearSelectionComponent, MonthSelectionComponent, WeatherStationMap,
        MinAvgMaxChart,
        SumChartComponent,
        WindDirectionChart,
        HeatmapChart,
        CustomizableDashboardComponent,
        ChartTileComponent,
        StationSelectorComponent,
        AirTemperatureChartComponent,
        DewPointTemperatureChartComponent,
        HumidityChartComponent,
        AirPressureChartComponent,
        VisibilityChartComponent,
        SunshineDurationChartComponent,
        AirTemperatureHeatmapChartComponent,
        SunshineDurationHeatmapChartComponent,
        RainChartComponent,
        SnowChartComponent,
        DropdownBoxComponent,
        CloudCoverageChartComponent,
        SunshineCloudCoverageHeatmapChartComponent,
    ],
    imports: [
        BrowserModule,
        RouterModule,
        FontAwesomeModule,
        LeafletModule,
        LeafletModule,
        FormsModule,
        HttpClientModule,
        HighchartsChartModule,
        MatSliderModule,
        MatSliderModule,
        MatSliderModule,
        MatInputModule,
        NgxSliderModule,
        KtdGridModule
    ],
    providers: [FilterService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
