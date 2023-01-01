import {DateTime} from "luxon";
import {environment} from "../environments/environment";
import {currentData, DateRangeFilter, SummaryData} from "./SummaryData";
import {WeatherStation} from "./WeatherStationService";
import {HttpClient} from "@angular/common/http";
import {ApplicationRef, Injectable} from "@angular/core";

@Injectable({
    providedIn: 'root'
})
export class FilterService {
    selectedStation?: WeatherStation
    dateRangeFilter: DateRangeFilter = DateRangeFilter.MONTHLY
    from: DateTime = DateTime.now()
    to: DateTime = DateTime.now()

    constructor(private http: HttpClient, private app: ApplicationRef) {
    }

    fireFilterChangedEvent(): void {
        if (this.selectedStation) {
            let stationId = this.selectedStation.id
            let url = ""
            if (this.dateRangeFilter === DateRangeFilter.MONTHLY) {
                let fromString = this.from?.startOf("month").toFormat("yyyy/MM")
                url = `${environment.apiServer}/stations/${stationId}/summary/${fromString}`
            } else if (this.dateRangeFilter === DateRangeFilter.YEARLY) {
                let fromString = this.from?.startOf("year").toFormat("yyyy")
                url = `${environment.apiServer}/stations/${stationId}/summary/${fromString}`
            } else if (this.dateRangeFilter === DateRangeFilter.LONG_TERM) {
                let fromString = this.from?.startOf("year").toFormat("yyyy")
                let toString = this.from?.endOf("year").toFormat("yyyy")
                url = `${environment.apiServer}/stations/${stationId}/summary/${fromString}-${toString}`
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

    get longTerm(): boolean {
        return this.dateRangeFilter == DateRangeFilter.LONG_TERM
    }

    get monthly(): boolean {
        return this.dateRangeFilter == DateRangeFilter.MONTHLY
    }

    get yearly(): boolean {
        return this.dateRangeFilter == DateRangeFilter.YEARLY
    }
}