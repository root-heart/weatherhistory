import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {NgSelectModule} from '@ng-select/ng-select';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CloudinessChart} from './charts/cloudiness-chart/cloudiness-chart.component';
import {StationAndDateFilterComponent} from './filter-header/station-and-date-filter.component';
import {MatTabsModule} from "@angular/material/tabs";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MinAvgMaxChart} from "./charts/MinAvgMaxChart";
import {SumChart} from "./charts/SumChart";
import {HistogramChart} from "./charts/HistogramChart";
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {LeafletModule} from "@asymmetrik/ngx-leaflet";
import {NgxSliderModule} from "@angular-slider/ngx-slider";
import { MapPopupComponent } from './filter-header/map-popup/map-popup.component';

@NgModule({
    declarations: [
        AppComponent, CloudinessChart, StationAndDateFilterComponent, MinAvgMaxChart, SumChart, HistogramChart, MapPopupComponent,
    ],
    imports: [
        LeafletModule, BrowserAnimationsModule, BrowserModule, HttpClientModule, NgSelectModule, FormsModule, ReactiveFormsModule, MatTabsModule, FontAwesomeModule, NgxSliderModule,
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
