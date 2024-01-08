import {AfterContentInit, Component, ContentChild, ElementRef, Input} from '@angular/core';
import {
    ConfigurationWizardStepSummaryComponent
} from "./configuration-wizard-step-summary.component";
import {
    ConfigurationWizardStepBodyComponent
} from "./configuration-wizard-step-body.component";

@Component({
  selector: 'wizard-step',
  template: '<ng-content></ng-content>'
})
export class ConfigurationWizardStepComponent  implements AfterContentInit {
    @ContentChild(ConfigurationWizardStepSummaryComponent)
    summary?: ConfigurationWizardStepSummaryComponent

    @ContentChild(ConfigurationWizardStepBodyComponent)
    body!: ConfigurationWizardStepBodyComponent

    @Input() caption: string = "<untitled>"

    @Input() enabled: boolean = true

    complete: boolean = false

    ngAfterContentInit() {
    }
}
