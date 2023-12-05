import {Component, OnInit} from '@angular/core';
import {FilterService} from "../../filter.service";
import {DateTime} from "luxon";
import {ChangeContext, PointerType} from "ngx-slider-v2";

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
        let intervals = this.filterService.getIntervals();
        if (intervals.length == 0 || intervals.length == 1 && intervals[0].start == 1 && intervals[0].end == 12) {
            return "Gesamtes Jahr"
        }
        return intervals
            .map(interval => interval.getString("MMM"))
            .join(", ")
    }

    updateFilterService(changeContext: ChangeContext) {
        console.log(changeContext)
        if (changeContext.pointerType == PointerType.Min) {
            this.filterService.year.next(changeContext.value)
        } else if (changeContext.pointerType == PointerType.Max) {
            this.filterService.endYear.next(changeContext.highValue!)
        } else {
            // TODO range drag
        }
    }

    fromChanged(event: Event) {
        this.filterService.year.next(parseInt((<HTMLInputElement>event.target).value))
    }

    toChanged(event: Event) {
        this.filterService.endYear.next(parseInt((<HTMLInputElement>event.target).value))
    }
}

