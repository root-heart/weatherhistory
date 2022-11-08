import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {SummaryChart} from "./summary/yearly/summary-chart";
import {NgSelectModule} from '@ng-select/ng-select';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Summary} from "./summary/yearly/summary";
import {TemperatureChart} from './summary/temperature-chart/temperature-chart.component';
import {SunshineChart} from './summary/sunshine-chart/sunshine-chart.component';
import {CloudinessChart} from './summary/cloudiness-chart/cloudiness-chart.component';
import {StationAndDateFilterComponent} from './filter-header/station-and-date-filter.component';
import { Meteogram } from './meteogram/meteogram.component';
import { PrecipitationChart } from './summary/precipitation-chart/precipitation-chart.component';
import { AirPressureChart } from './summary/air-pressure-chart/air-pressure-chart.component';

@NgModule({
    declarations: [
        AppComponent, SummaryChart, Summary, TemperatureChart, SunshineChart, CloudinessChart, StationAndDateFilterComponent, Meteogram, TemperatureChart, TemperatureChart, TemperatureChart, PrecipitationChart, AirPressureChart
    ],
    imports: [
        BrowserModule, HttpClientModule, NgSelectModule, FormsModule, ReactiveFormsModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
