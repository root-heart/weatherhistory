import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {SummaryChart} from "./charts/yearly/summary-chart";
import {NgSelectModule} from '@ng-select/ng-select';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Summary} from "./charts/yearly/summary";
import {TemperatureChart} from './charts/temperature-chart/temperature-chart.component';
import {SunshineChart} from './charts/sunshine-chart/sunshine-chart.component';
import {CloudinessChart} from './charts/cloudiness-chart/cloudiness-chart.component';
import {StationAndDateFilterComponent} from './filter-header/station-and-date-filter.component';
import { PrecipitationChart } from './charts/precipitation-chart/precipitation-chart.component';
import { AirPressureChart } from './charts/air-pressure-chart/air-pressure-chart.component';

@NgModule({
    declarations: [
        AppComponent, SummaryChart, Summary, TemperatureChart, SunshineChart, CloudinessChart, StationAndDateFilterComponent, TemperatureChart, TemperatureChart, TemperatureChart, PrecipitationChart, AirPressureChart
    ],
    imports: [
        BrowserModule, HttpClientModule, NgSelectModule, FormsModule, ReactiveFormsModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
