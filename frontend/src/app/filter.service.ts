import {DateTime, MonthNumbers} from "luxon";
import {environment} from "../environments/environment";
import {currentData, DateRangeFilter, SummaryData} from "./SummaryData";
import {WeatherStation} from "./WeatherStationService";
import {HttpClient} from "@angular/common/http";
import {ApplicationRef, Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {ChartResolution} from "./charts/BaseChart";

export type Month = ("jan" | "feb" | "mar" | "apr" | "may" | "jun" | "jul" | "aug" | "sep" | "oct" | "nov" | "dec")
export type Season = ("3-5" | "6-8" | "9-11" | "12-2")

export type DateRangeIdentifier = MonthNumbers | Season | "year" | "multipleYears"

@Injectable({
    providedIn: 'root'
})
export class FilterService {
    selectedStation?: WeatherStation
    dateRangeIdentifier: BehaviorSubject<DateRangeIdentifier> = new BehaviorSubject<DateRangeIdentifier>(1)
    year: BehaviorSubject<number> = new BehaviorSubject(2022)
    endYear: BehaviorSubject<number> = new BehaviorSubject(2022)

    constructor(private http: HttpClient, private app: ApplicationRef) {
        this.dateRangeIdentifier.subscribe(r => this.fireFilterChangedEvent())
        this.year.subscribe(r => this.fireFilterChangedEvent())
        this.endYear.subscribe(r => this.fireFilterChangedEvent())
    }

    fireFilterChangedEvent(): void {
        if (this.selectedStation) {
            let stationId = this.selectedStation.id
            let url = `${environment.apiServer}/stations/${stationId}/summary/`
            if (this.dateRangeIdentifier.value == "year") {
                url += `${this.year.value}`
            } else if (this.dateRangeIdentifier.value == "multipleYears") {
                url += `${this.year.value}-${this.endYear.value}`
            } else {
                url += `${this.year.value}/${this.dateRangeIdentifier.value}`
            }
            console.log(`fetching data from ${url}`)
            this.http
                .get<SummaryData>(url)
                .subscribe(data => {
                    currentData.next(data)
                    // hmm, something in angular does not work, so i have to refresh everything on my own here...
                    this.app.tick()
                })
        }
    }

    public getChartResolution(): ChartResolution {
        if (this.dateRangeIdentifier.value == "multipleYears") {
            return "yearly"
        } else if (this.dateRangeIdentifier.value == "year") {
            return "monthly"
        } else {
            return "daily"
        }
    }

}

