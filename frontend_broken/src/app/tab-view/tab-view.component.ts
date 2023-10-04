import {
    AfterContentInit,
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ContentChild,
    ContentChildren,
    ElementRef,
    Input, Output,
    QueryList,
    TemplateRef,
    ViewChild, ViewChildren
} from '@angular/core';
import {MinAvgMaxChart} from "../charts/MinAvgMaxChart";
import {SumChart} from "../charts/SumChart";
import {Browser} from "leaflet";
import chrome = Browser.chrome;

@Component({
    selector: 'tab-button',
    template: `
        <button #element>
            <ng-content></ng-content>
        </button>`
})
export class TabButton {
    @ViewChild("element") htmlElement!: ElementRef<HTMLButtonElement>
}

@Component({selector: 'tab-pane', template: "<ng-content></ng-content>"})
export class TabPane {}

@Component({
    selector: 'tab',
    template: `
        <ng-content select="tab-button"></ng-content>


        <ng-template #abc>
            <ng-content select="tab-pane"></ng-content>
        </ng-template>

    `
})
export class Tab implements AfterViewInit {
    @Input() id!: string
    @ContentChild(TabButton) button!: TabButton
    @ContentChild(TabPane) pane!: TabPane
    @ViewChild("abc") template!: TemplateRef<any>
    @Input() onActivate!: (tab: Tab) => void
    @ViewChild(MinAvgMaxChart) chart?: MinAvgMaxChart

    visible: boolean = false

    ngAfterViewInit(): void {
        if (this.chart !== undefined) {
            console.log(this.chart)
            this.chart.updateChart()
        }
    }

}

@Component({
    selector: 'tab-view',
    template: `
        <div class="tab-header-bar">
            <ng-content select="tab"></ng-content>
        </div>

        <div class="tab-pane-container" style="background-color: #666; height: 30rem">
            <div *ngFor="let tab of tabs">
                <div [style.display]="tab === activeTab ? 'block' : 'none'" [style.height]="'20rem'">
                    <ng-container *ngTemplateOutlet="tab.template"></ng-container>
                </div>
                
            </div>
        </div>
    `,
    styleUrls: ['./tab-view.component.css']
})
export class TabView implements AfterContentInit, AfterViewInit {
    @ContentChildren(Tab) tabs!: QueryList<Tab>

    activeTab?: Tab

    constructor(private changeDetector: ChangeDetectorRef) {
    }

    ngAfterContentInit() {
        if (this.tabs.length > 0) {
            this.activateTab(this.tabs.first)
        }
    }

    ngAfterViewInit() {
        if (this.tabs.length > 0) {
            this.tabs.forEach(tab =>
                tab.button.htmlElement.nativeElement
                    .addEventListener("click", e => this.activateTab(tab)))
        }
        this.changeDetector.detectChanges()
    }

    activateTab(tab: Tab) {
        if (this.activeTab != null) {
            this.activeTab.visible = false
        }
        tab.visible = true
        this.activeTab = tab
        this.changeDetector.detectChanges()
        if (tab.onActivate) {
            tab.onActivate(tab)
        }
    }
}
