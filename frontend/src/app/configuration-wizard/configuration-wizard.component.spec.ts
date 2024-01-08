import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigurationWizardComponent } from './configuration-wizard.component';

describe('ConfigurationWizardComponent', () => {
  let component: ConfigurationWizardComponent;
  let fixture: ComponentFixture<ConfigurationWizardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ConfigurationWizardComponent]
    });
    fixture = TestBed.createComponent(ConfigurationWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
