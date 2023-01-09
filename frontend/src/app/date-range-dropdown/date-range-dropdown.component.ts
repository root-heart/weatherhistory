import {Component, OnInit} from '@angular/core';
import {
    faAngleLeft,
    faAngleRight,
    faAnglesLeft,
    faAnglesRight,
    faCalendarDays
} from "@fortawesome/free-solid-svg-icons";
import {FilterService} from "../filter.service";

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


    constructor(public filterService: FilterService) {
    }

    ngOnInit(): void {
    }

    addToYears(value: (-10 | -1 | 1 | 10)) {
        this.filterService.year.next(this.filterService.year.value + value)
    }


}

