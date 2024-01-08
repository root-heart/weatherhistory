import {Component, Input, TemplateRef, ViewChild} from '@angular/core';

@Component({
  selector: 'wizard-step-summary',
  template: '<ng-template><ng-content></ng-content></ng-template>'
})
export class ConfigurationWizardStepSummaryComponent {
    @ViewChild(TemplateRef) content!: TemplateRef<any>

}
