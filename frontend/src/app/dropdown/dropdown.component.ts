import {Component, OnInit} from '@angular/core';
import {DropdownService, DropdownX} from "../dropdown.service";

@Component({
    selector: 'dropdown',
    templateUrl: './dropdown.component.html',
    styleUrls: ['./dropdown.component.css']
})
export class Dropdown implements OnInit, DropdownX {
    private dv: boolean = false

    set dropdownVisible(v: boolean) {
        this.dv = v
        console.log(v)
    }

    get dropdownVisible(): boolean {
        return this.dv
    }

    constructor() {
    }

    ngOnInit(): void {
    }

    hide() {
        this.dv = false
    }
}
