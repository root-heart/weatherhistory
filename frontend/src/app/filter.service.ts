import * as luxon from "luxon";
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
    dateRangeFilter: DateRangeFilter = DateRangeFilter.THIS_MONTH
    from?: number
    to?: number

    constructor(private http: HttpClient, private app: ApplicationRef) {
    }

    fireFilterChangedEvent(): void {
        if (this.selectedStation) {
            let stationId = this.selectedStation.id
            let url = ""
            if (this.dateRangeFilter === DateRangeFilter.THIS_MONTH) {
                let from = luxon.DateTime.now().startOf("month").toFormat("yyyy/MM")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from}`
            } else if (this.dateRangeFilter === DateRangeFilter.LAST_MONTH) {
                let from = luxon.DateTime.now().minus({month: 1}).startOf("month").toFormat("yyyy/MM")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from}`
            } else if (this.dateRangeFilter === DateRangeFilter.THIS_YEAR) {
                let from = luxon.DateTime.now().startOf("year").toFormat("yyyy")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from}`
            } else if (this.dateRangeFilter === DateRangeFilter.LAST_YEAR) {
                let from = luxon.DateTime.now().minus({year: 1}).startOf("year").toFormat("yyyy")
                url = `${environment.apiServer}/stations/${stationId}/summary/${from}`
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
}