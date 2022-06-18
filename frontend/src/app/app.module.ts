import { MbscModule } from '@mobiscroll/angular-lite';
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
import { DatePartChooserComponent } from './date-part-chooser/date-part-chooser.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {MatDividerModule} from "@angular/material/divider";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MAT_DATE_LOCALE, MatNativeDateModule} from '@angular/material/core';

@NgModule({
    declarations: [
        AppComponent, SummaryChart, Summary, TemperatureChart, SunshineChart, CloudinessChartComponent, StationAndDateFilterComponent, DatePartChooserComponent
    ],
    imports: [
        MbscModule, MatNativeDateModule,
        BrowserModule, HttpClientModule, NgSelectModule, FormsModule, DpDatePickerModule, ReactiveFormsModule, MatCheckboxModule, BrowserAnimationsModule, MatAutocompleteModule, MatInputModule, MatSelectModule, MatDividerModule, MatDatepickerModule
    ],
  providers: [{provide: MAT_DATE_LOCALE, useValue: 'de-DE'},],
  bootstrap: [AppComponent]
})
export class AppModule {
}
