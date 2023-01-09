import {Component, Input, OnInit} from '@angular/core';
import {DateRangeIdentifier, FilterService} from "../filter.service";

@Component({
    selector: 'toggable-button',
    templateUrl: './toggable-button.component.html',
    styleUrls: ['./toggable-button.component.css']
})
export class ToggableButtonComponent implements OnInit {
    @Input()
    value?: DateRangeIdentifier

    constructor(private filterService: FilterService) {
    }

    ngOnInit(): void {

    }

    clicked(): void {
        if (this.value) {
            this.filterService.dateRangeIdentifier.next(this.value)
        }
    }

    isCurrentlySelected(): boolean {
        return this.filterService.dateRangeIdentifier.value === this.value
    }
}
