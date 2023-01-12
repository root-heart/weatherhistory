import {Component, OnInit} from '@angular/core';
import {FilterService} from "../filter.service";
import {DateTime} from "luxon";

@Component({
    selector: 'date-range-selector',
    templateUrl: './date-range-selector.component.html',
    styleUrls: ['./date-range-selector.component.css']
})
export class DateRangeSelector implements OnInit {
    constructor(public filterService: FilterService) {
    }

    ngOnInit(): void {
    }

    getMonthName(monthNumber: number): string {
        return DateTime.fromObject({month: monthNumber}).toFormat("MMMM", {locale: "de-DE"})
    }

    getButtonName(): string {
        if (this.filterService.wholeYear.value) {
            return "Gesamtes Jahr"
        }
        return this.filterService.getIntervals()
            .map(interval => interval.getString("MMM"))
            .join(", ")
    }

}

