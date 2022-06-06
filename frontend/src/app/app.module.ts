import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {SummaryChart} from "./summary/yearly/summary-chart";
import { NgSelectModule } from '@ng-select/ng-select';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DpDatePickerModule} from 'ng2-date-picker';
import {Summary} from "./summary/yearly/summary";
import { TemperatureChart } from './summary/temperature-chart/temperature-chart.component';
import { SunshineChart } from './summary/sunshine-chart/sunshine-chart.component';
import { CloudinessChartComponent } from './summary/cloudiness-chart/cloudiness-chart.component';
import { StationAndDateFilterComponent } from './filter-header/station-and-date-filter.component';

@NgModule({
    declarations: [
        AppComponent, SummaryChart, Summary, TemperatureChart, SunshineChart, CloudinessChartComponent, StationAndDateFilterComponent
    ],
    imports: [
        BrowserModule, HttpClientModule, NgSelectModule, FormsModule, DpDatePickerModule, ReactiveFormsModule
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
