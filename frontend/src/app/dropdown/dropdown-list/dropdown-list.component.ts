import {Component, ElementRef, EventEmitter, Input, Output} from '@angular/core';

@Component({
    selector: 'dropdown-list',
    templateUrl: './dropdown-list.component.html',
    styleUrls: ['./dropdown-list.component.scss'],
    host: {
        "(document:click)": "onclick($event)"
    }
})
export class DropdownList<T extends { toString: () => string }> {
    @Input() elements: T[] = []
    @Input() selectedElement: T | null = null
    @Output() selectedElementChange = new EventEmitter<T>()
    @Input() elementToString = (element: T) => element.toString()

    constructor(private elementRef: ElementRef) {
    }

    selectElement(element: T) {
        this.selectedElementChange.emit(element)
        this.dropdownVisible = false
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
