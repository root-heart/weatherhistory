import {Component, ElementRef, OnInit} from '@angular/core';

@Component({
    selector: 'dropdown',
    templateUrl: './dropdown.component.html',
    styleUrls: ['./dropdown.component.css'],
    host: {
        "(document:click)": "onclick($event)"
    }
})
export class Dropdown {
    private _dropdownVisible: boolean = false

    constructor(private elementRef: ElementRef) {
    }

    get dropdownVisible(): boolean {
        return this._dropdownVisible
    }

    set dropdownVisible(v: boolean) {
        this._dropdownVisible = v
    }

    onclick(event: Event) {
        if (!this.elementRef.nativeElement.contains(event.target)) {
            this._dropdownVisible = false
        }
    }

}
