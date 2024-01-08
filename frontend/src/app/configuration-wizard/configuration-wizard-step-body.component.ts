import {Component, TemplateRef, ViewChild} from '@angular/core';

@Component({
  selector: 'wizard-step-body',
  template: '<ng-template><ng-content></ng-content></ng-template>'
})
export class ConfigurationWizardStepBodyComponent {
    @ViewChild(TemplateRef) content!: TemplateRef<any>
}
