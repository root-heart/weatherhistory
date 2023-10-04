import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FilterService} from "../../../filter.service";
import {faAngleLeft, faAngleRight, faAnglesLeft, faAnglesRight} from "@fortawesome/free-solid-svg-icons";
import {ControlValueAccessor} from "@angular/forms";
import {BehaviorSubject} from "rxjs";

@Component({
    selector: 'year-selection',
    templateUrl: './year-selection.component.html',
    styleUrls: ['./year-selection.component.css']
})
export class YearSelectionComponent {

    bigMinus = faAnglesLeft
    minus = faAngleLeft
    bigPlus = faAnglesRight
    plus = faAngleRight

    yearValue?: number
    private yearModel?: BehaviorSubject<number>

    @Input() set year(s: BehaviorSubject<number> | undefined) {
        this.yearModel = s
        s?.subscribe(v => this.yearValue = v)
    }

    get year(): BehaviorSubject<number> | undefined {
        return this.yearModel
    }

    constructor() {
    }

    addToYears(value: (-10 | -1 | 1 | 10)) {
        if (this.yearValue) {
            this.yearValue += value
            this.year?.next(this.yearValue)
        }
    }

}
