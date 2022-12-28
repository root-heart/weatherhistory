import {Component, ViewChild} from '@angular/core';
import {CloudinessChart} from "./charts/cloudiness-chart/cloudiness-chart.component";
import {
    faCalendar,
    faCloud,
    faCloudShowersHeavy,
    faCloudSun,
    faMapLocationDot,
    faSnowflake,
    faSun
} from '@fortawesome/free-solid-svg-icons';
import {currentData, currentFilter, DateRangeFilter} from "./SummaryData";

export type MeasurementTypes = "temperature" | "humidity" | "airPressure" | "visibility"

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
})
export class AppComponent {
    title = 'wetterchroniken.de/';

    @ViewChild('cloudinessChart')
    cloudinessChart?: CloudinessChart

    measurementType?: MeasurementTypes

    faSun = faSun
    faCloudSun = faCloudSun
    faCloud = faCloud
    faRain = faCloudShowersHeavy
    faSnow = faSnowflake
    faMapLocationDot = faMapLocationDot
    faCalendar = faCalendar
    currentData = currentData
    currentFilter = currentFilter

    visibleFilter?: any = undefined

    showDetails(measurementType: MeasurementTypes) {
        this.measurementType = measurementType
    }

    showOrHideFilter(filterComponent?: any) {
        console.log("showOrHideFilter")
        if (this.visibleFilter == filterComponent) {
            this.visibleFilter = undefined
        } else {
            this.visibleFilter = filterComponent
        }
    }

    divideBy60(x?: number): number | undefined {
        return x ? x / 60 : undefined
    }

    percentageOfCloudCoverage(coverageHistogram: number[] | undefined, coverageIndices: number[]): string {
        if (!coverageHistogram) {
            return ""
        }

        let part = 0
        let sum = 0
        for (let i = 0; i < coverageHistogram.length; i++) {
            if (coverageIndices.indexOf(i) !== -1) {
                part += coverageHistogram[i]
            }
            sum += coverageHistogram[i]
        }
        return (part / sum * 100).toFixed(1) + "%"
    }

    getFilterButtonCaption(): string {
        let caption = "Bitte Ort und Zeitraum wählen..."
        if (currentFilter.selectedStation) {
            caption = currentFilter.selectedStation.name
            if (currentFilter.dateRangeFilter === DateRangeFilter.THIS_MONTH) {
                caption += " für diesen Monat"
            } else if (currentFilter.dateRangeFilter === DateRangeFilter.LAST_MONTH) {
                caption += " für letzten Monat"
            } else if (currentFilter.dateRangeFilter === DateRangeFilter.THIS_YEAR) {
                caption += " für dieses Jahr"
            } else if (currentFilter.dateRangeFilter === DateRangeFilter.LAST_YEAR) {
                caption += " für letztes Jahr"
            } else if (currentFilter.from) {
                if (currentFilter.to) {
                    caption += " von " + currentFilter.from + " bis " + currentFilter.to
                } else {
                    caption += " im Jahr " + currentFilter.from
                }
            }
        }
        return caption
    }
}

