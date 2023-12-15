import {Component, ElementRef} from '@angular/core';

@Component({
    selector: 'app-dropdown-box',
    templateUrl: './dropdown-box.component.html',
    styleUrls: ['./dropdown-box.component.css'],
    host: {
        "(document:click)": "onclick($event)"
    }
})
export class DropdownBoxComponent {
    constructor(private elementRef: ElementRef) {
    }

    private _dropdownVisible: boolean = false

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
