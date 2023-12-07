import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {DateRangeSelector} from "./navigation/date-range-dropdown/date-range-selector.component";
import {FilterService} from "./filter.service";
import {YearSelectionComponent} from "./navigation/date-range-dropdown/year-selection/year-selection.component";
import {Dropdown} from './dropdown/dropdown.component';
import {MonthSelectionComponent} from "./navigation/date-range-dropdown/month-selection/month-selection.component";
import {ToggableButtonComponent} from './toggable-button/toggable-button.component';
import {StationChartsComponent} from "./station-charts/station-charts.component";

import {AppComponent} from './app.component';
import {RouterModule} from "@angular/router";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {MapDropdown} from "./map-dropdown/weather-station-map.component";
import {LeafletModule} from "@asymmetrik/ngx-leaflet";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from '@angular/common/http';
import {HighchartsChartModule} from "highcharts-angular";
import { MinAvgMaxChart } from './charts/min-avg-max-chart/min-avg-max-chart.component';
import { SumChartComponent } from './charts/sum-chart/sum-chart.component';
import {MatSliderModule} from "@angular/material/slider";
import {MatInputModule} from "@angular/material/input";
import {NgxSliderModule} from "ngx-slider-v2";
import { WindDirectionChart } from './charts/wind-direction-chart/wind-direction-chart.component';


@NgModule({
    declarations: [
        AppComponent,
        ToggableButtonComponent, Dropdown, DateRangeSelector,
        YearSelectionComponent, MonthSelectionComponent, MapDropdown, StationChartsComponent,
        MinAvgMaxChart,
        SumChartComponent,
        WindDirectionChart
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
        NgxSliderModule
    ],
    providers: [FilterService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
