import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {NgSelectModule} from '@ng-select/ng-select';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SunshineChart} from './charts/sunshine-chart/sunshine-chart.component';
import {CloudinessChart} from './charts/cloudiness-chart/cloudiness-chart.component';
import {StationAndDateFilterComponent} from './filter-header/station-and-date-filter.component';
import {PrecipitationChart} from './charts/precipitation-chart/precipitation-chart.component';
import {WindSpeedChart} from './charts/wind-speed-chart/wind-speed-chart.component';
import {MatTabsModule} from "@angular/material/tabs";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MinAvgMaxChart} from "./charts/MinAvgMaxChart";

@NgModule({
    declarations: [
        AppComponent, SunshineChart, CloudinessChart, StationAndDateFilterComponent, PrecipitationChart, WindSpeedChart, MinAvgMaxChart,
    ],
    imports: [
        BrowserAnimationsModule, BrowserModule, HttpClientModule, NgSelectModule, FormsModule, ReactiveFormsModule, MatTabsModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
