import {AfterContentInit, Component, ContentChildren, EventEmitter, Output, QueryList} from '@angular/core';
import {ConfigurationWizardStepComponent} from "./configuration-wizard-step.component";

@Component({
  selector: 'configuration-wizard',
  templateUrl: './configuration-wizard.component.html',
  styleUrls: ['./configuration-wizard.component.scss']
})
export class ConfigurationWizardComponent implements AfterContentInit {
    @ContentChildren(ConfigurationWizardStepComponent) steps!: QueryList<ConfigurationWizardStepComponent>

    currentStep?: ConfigurationWizardStepComponent
    @Output() cancelled = new EventEmitter<void>()
    @Output() confirmed = new EventEmitter<void>()

    ngAfterContentInit() {
        this.currentStep = this.steps.get(0)
    }

    stepForward() {
        let index = this.determineCurrentStepIndex();
        if (index < this.steps.length - 1) {
            this.currentStep = this.steps.get(index + 1)
        }
    }

    stepBack() {
        let index = this.determineCurrentStepIndex();
        if (index > 0) {
            this.currentStep = this.steps.get(index - 1)
        }
    }

    cancel() {
        this.cancelled.emit()
    }

    confirm() {
        this.confirmed.emit()
    }

    isLastStep() {
        return this.determineCurrentStepIndex() == this.steps.length - 1
    }

    private determineCurrentStepIndex() {
        let currentStepIndex = 0
        this.steps.forEach((step, index) => {
            if (step == this.currentStep) {
                currentStepIndex = index
            }
        })
        return currentStepIndex
    }
}
