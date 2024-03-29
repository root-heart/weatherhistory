import {DateTime} from "luxon";
import {SummaryData} from "./data-classes";
import {WeatherStation} from "./WeatherStationService";
import {HttpClient} from "@angular/common/http";
import {ApplicationRef, Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {environment} from "./environments/environment";

@Injectable({
    providedIn: 'root'
})
export class FilterService {
    selectedStation?: WeatherStation
    year: BehaviorSubject<number> = new BehaviorSubject(2022)
    endYear: BehaviorSubject<number> = new BehaviorSubject(2022)
    wholeYear: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true)
    months = Array<BehaviorSubject<boolean>>(12)
    currentData = new BehaviorSubject<SummaryData | undefined>(undefined)

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
                let monthNumbers = this.getIntervals()
                    .map(interval => interval.getNumbersString())
                    .join(",")
                url += `/${monthNumbers}`
            }
            console.log(`fetching data from ${url}`)
            this.http
                .get<SummaryData>(url)
                .subscribe(data => {
                    // console.log(data.details.map(x => x.measurements))
                    this.currentData.next(data)
                    // hmm, something in angular does not work, so i have to refresh everything on my own here...
                    this.app.tick()
                })
        }
    }

    public getIntervals(): MonthInterval[] {
        let interval = new MonthInterval(0, 0)
        let intervals: MonthInterval[] = []
        for (let i = 0; i < 12; i++) {
            if (this.months[i].value && interval.start == 0) {
                interval.start = i + 1
            } else if (!this.months[i].value && interval.start != 0) {
                interval.end = i
                intervals.push(interval)
                interval = new MonthInterval(0, 0)
            }
        }
        if (interval.start > 0) {
            interval.end = 12
            intervals.push(interval)
        }
        return intervals
    }

}

export class MonthInterval {
    start: number
    end: number


    constructor(start: number, end: number) {
        this.start = start;
        this.end = end;
    }

    getNumbersString(): string {
        if (this.start == this.end) {
            return "" + this.start
        } else {
            return `${this.start}-${this.end}`
        }
    }

    getString(monthNamePattern: string): string {
        if (this.start == this.end) {
            return MonthInterval.getMonthName(this.start, monthNamePattern)
        } else if (this.end == this.start + 1) {
            return MonthInterval.getMonthName(this.start, monthNamePattern) + ", " + MonthInterval.getMonthName(this.end, monthNamePattern)
        } else {
            return MonthInterval.getMonthName(this.start, monthNamePattern) + " - " + MonthInterval.getMonthName(this.end, monthNamePattern)
        }
    }

    static getMonthName(monthNumber: number, monthNamePattern: string): string {
        return DateTime.fromObject({month: monthNumber}).toFormat(monthNamePattern, {locale: "de-DE"})
    }

}
