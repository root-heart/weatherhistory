import {Component, OnInit} from '@angular/core';
import {FilterService} from "../filter.service";
import {DateTime, MonthNumbers} from "luxon";

@Component({
    selector: 'date-range-dropdown',
    templateUrl: './date-range-dropdown.component.html',
    styleUrls: ['./date-range-dropdown.component.css']
})
export class DateRangeDropdownComponent implements OnInit {
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
        return this.filterService.months
                .map((s, index) => s.value ? (index + 1) : null)
                .filter(i => i !== null)
                .map(monthNumber => DateTime.fromObject({month: monthNumber ?? 0}))
                .map(dateTime => dateTime.toFormat("MMM", {locale: "de-DE"}))
                .join(",")
    }
}

