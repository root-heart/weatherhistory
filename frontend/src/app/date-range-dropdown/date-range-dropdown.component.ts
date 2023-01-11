import {Component, OnInit} from '@angular/core';
import {
    faAngleLeft,
    faAngleRight,
    faAnglesLeft,
    faAnglesRight,
    faCalendarDays
} from "@fortawesome/free-solid-svg-icons";
import {FilterService} from "../filter.service";
import {DateTime} from "luxon";

@Component({
    selector: 'date-range-dropdown',
    templateUrl: './date-range-dropdown.component.html',
    styleUrls: ['./date-range-dropdown.component.css']
})
export class DateRangeDropdownComponent implements OnInit {
    faCalendar = faCalendarDays

    bigMinus = faAnglesLeft
    minus = faAngleLeft
    bigPlus = faAnglesRight
    plus = faAngleRight

    year1: number = 2000
    year2: number = 2002

    constructor(public filterService: FilterService) {
    }

    ngOnInit(): void {
    }


    getButtonCaption(): string {
        if (this.filterService.dateRangeIdentifier.value == "multipleYears") {
            return `von ${this.filterService.year.value} bis ${this.filterService.endYear.value}`
        } else if (this.filterService.dateRangeIdentifier.value == "year") {
            return `Jahr ${this.filterService.year.value}`
        } else {
            let monthName = DateTime.fromObject({month: this.filterService.dateRangeIdentifier.value as number}).toFormat("MMMM", {locale: "de-DE"})
            return `${monthName} ${this.filterService.year.value}`
        }
    }
}

