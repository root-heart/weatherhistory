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
        this.dropdownService.currentlyVisibleDropdown = v ? this : undefined
        if (this.dropdownService.dropdownBackground) {
            this.dropdownService.dropdownBackground.nativeElement.style.visibility = v ? "visible" : "hidden"
        }
        this.dv = v
    }

    get dropdownVisible(): boolean {
        return this.dv
    }

    constructor(private dropdownService: DropdownService) {
    }

    ngOnInit(): void {
    }

    hide() {
        this.dv = false
    }
}
