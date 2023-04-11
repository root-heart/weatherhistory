import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {NgSelectModule} from '@ng-select/ng-select';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CloudinessChart} from './charts/cloudiness-chart/cloudiness-chart.component';
import {MatTabsModule} from "@angular/material/tabs";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MinAvgMaxChart} from "./charts/MinAvgMaxChart";
import {SumChart} from "./charts/SumChart";
import {HistogramChart} from "./charts/HistogramChart";
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {LeafletModule} from "@asymmetrik/ngx-leaflet";
import {NgxSliderModule} from "@angular-slider/ngx-slider";
import {FilterService} from "./filter.service";
import {Dropdown} from './dropdown/dropdown.component';
import {DateRangeSelector} from './date-range-dropdown/date-range-selector.component';
import {MapDropdown} from "./map-dropdown/map-dropdown.component";
import {ToggableButtonComponent} from './toggable-button/toggable-button.component';
import {RangeSliderComponent} from './range-slider/range-slider.component';
import {YearSelectionComponent} from './year-selection/year-selection.component';
import {MonthSelectionComponent} from './month-selection/month-selection.component';
import {WeatherStationSelectorComponent} from './weather-station-selector/weather-station-selector.component';
import {TabView, Tab, TabButton, TabPane} from './tab-view/tab-view.component';
import { NavigationComponent } from './navigation/navigation.component';
import { StationChartsComponent } from './station-charts/station-charts.component';

@NgModule({
    declarations: [
        AppComponent, CloudinessChart, MapDropdown, MinAvgMaxChart, SumChart, HistogramChart, Dropdown, DateRangeSelector, ToggableButtonComponent, RangeSliderComponent, YearSelectionComponent, MonthSelectionComponent, WeatherStationSelectorComponent, TabView, Tab, TabButton, TabPane, NavigationComponent, StationChartsComponent
    ],
    imports: [
        LeafletModule, BrowserAnimationsModule, BrowserModule, HttpClientModule, NgSelectModule, FormsModule, ReactiveFormsModule, MatTabsModule, FontAwesomeModule, NgxSliderModule
    ],
    providers: [FilterService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
