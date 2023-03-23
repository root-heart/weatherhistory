import {ElementRef, Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class DropdownService {
    private dropdown?: DropdownX
    private background?: ElementRef

    set currentlyVisibleDropdown(c: DropdownX | undefined) {
        if (this.dropdown) {
            this.dropdown.hide()
        }
        this.dropdown = c
        if (this.background) {
            this.background.nativeElement.style.visibility = c ? "visible" : "hidden"
        }
    }

    set dropdownBackground(r: ElementRef | undefined) {
        if (r) {
            r.nativeElement.style.visibility = "hidden"
            r.nativeElement.onclick = (e: MouseEvent) => {
                if (this.dropdown) {
                    this.dropdown.hide()
                    this.dropdown = undefined
                    r.nativeElement.style.visibility = "hidden"
                }
            }
        }
        this.background = r
    }

    constructor() {
    }
}


export interface DropdownX {
    hide(): void
}