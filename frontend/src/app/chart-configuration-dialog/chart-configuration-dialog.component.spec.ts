import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChartConfigurationDialog } from './chart-configuration-dialog.component';

describe('ChartConfigurationDialogComponent', () => {
  let component: ChartConfigurationDialog;
  let fixture: ComponentFixture<ChartConfigurationDialog>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ChartConfigurationDialog]
    });
    fixture = TestBed.createComponent(ChartConfigurationDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
