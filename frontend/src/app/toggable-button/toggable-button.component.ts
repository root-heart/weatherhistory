import {Component, Input} from '@angular/core';
import {faSquare, faSquareCheck} from "@fortawesome/free-solid-svg-icons";
import {BehaviorSubject} from "rxjs";

@Component({
    selector: 'toggable-button',
    templateUrl: './toggable-button.component.html',
    styleUrls: ['./toggable-button.component.css']
})
export class ToggableButtonComponent {
    @Input()
    subject?: BehaviorSubject<boolean>

    @Input()
    enabled?: boolean = true

    checked = faSquareCheck
    unchecked = faSquare

    constructor() {
    }

    clicked(): void {
        if (this.enabled) {
            this.subject?.next(!this.subject.value)
        }
    }

    isCurrentlySelected(): boolean {
        return this.subject?.value ?? false;
    }
}
