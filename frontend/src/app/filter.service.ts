import {DateTime, MonthNumbers} from "luxon";
import {environment} from "../environments/environment";
import {currentData, DateRangeFilter, SummaryData} from "./SummaryData";
import {WeatherStation} from "./WeatherStationService";
import {HttpClient} from "@angular/common/http";
import {ApplicationRef, Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {ChartResolution} from "./charts/BaseChart";


@Injectable({
    providedIn: 'root'
})
export class FilterService {
    selectedStation?: WeatherStation
    year: BehaviorSubject<number> = new BehaviorSubject(2022)
    endYear: BehaviorSubject<number> = new BehaviorSubject(2022)
    wholeYear: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true)
    months = Array<BehaviorSubject<boolean>>(12)

    constructor(private http: HttpClient, private app: ApplicationRef) {
        this.wholeYear.subscribe(r => this.fireFilterChangedEvent())
        this.year.subscribe(r => this.fireFilterChangedEvent())
        this.endYear.subscribe(r => this.fireFilterChangedEvent())
        for (let i = 0; i < 12; i++) {
            this.months[i] = new BehaviorSubject<boolean>(false)
        }
        this.months.forEach(m => m.subscribe(r => this.fireFilterChangedEvent()))
    }

    fireFilterChangedEvent(): void {
        if (this.selectedStation) {
            let stationId = this.selectedStation.id
            let url = `${environment.apiServer}/stations/${stationId}/summary/${this.year.value}`
            if (this.endYear.value !== this.year.value) {
                url += `-${this.endYear.value}`
            }

            if (!this.wholeYear.value) {
                let monthNumbers = this.months
                    .map((s, index) => s.value ? "" + (index + 1) : null)
                    .filter(i => i !== null)
                    .join(",")
                url += `/${monthNumbers}`
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
        if (this.year.value != this.endYear.value) {
            // return "yearly"
            // } else if (this.dateRangeIdentifier.value == "year") {
            return "monthly"
        } else {
            return "daily"
        }
    }

}

