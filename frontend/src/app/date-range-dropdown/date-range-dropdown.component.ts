import {Component, OnInit} from '@angular/core';
import {
    faAngleLeft,
    faAngleRight,
    faAnglesLeft, faAnglesRight, faCalendarDays,
    faCalendarWeek,
    faChevronLeft, faChevronRight,
    faCircleCheck,
    faCircleDot,
    faSquare,
    faSquareXmark
} from "@fortawesome/free-solid-svg-icons";
import {DateRangeFilter} from "../SummaryData";
import {FilterService} from "../filter.service";
import {faCircle} from "@fortawesome/free-solid-svg-icons/faCircle";

@Component({
    selector: 'date-range-dropdown',
    templateUrl: './date-range-dropdown.component.html',
    styleUrls: ['./date-range-dropdown.component.css']
})
export class DateRangeDropdownComponent implements OnInit {
    unchecked = faCircle
    checked = faCircleDot
    faCalendar = faCalendarDays

    bigMinus = faAnglesLeft
    minus = faAngleLeft
    bigPlus = faAnglesRight
    plus = faAngleRight

    DateRangeFilter = DateRangeFilter

    constructor(public filterService: FilterService) {
    }

    ngOnInit(): void {
    }

    getDecades(): string[] {
        return ["1970 - 1979", "1980 - 1989", "1990 - 1999", "2000 - 2009", "2010 - 2019", "ab 2020"]
    }

    getYears(): number[] {
        let start = 1900
        let end = 2024
        return Array.from({length: (end - start)}, (v, k) => k + start)
    }

    filter(range: DateRangeFilter) {
        this.filterService.dateRangeFilter = range
        this.filterService.fireFilterChangedEvent()
    }

}
