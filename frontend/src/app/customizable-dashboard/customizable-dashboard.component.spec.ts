import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomizableDashboardComponent } from './customizable-dashboard.component';

describe('CustomizableDashboardComponent', () => {
  let component: CustomizableDashboardComponent;
  let fixture: ComponentFixture<CustomizableDashboardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CustomizableDashboardComponent]
    });
    fixture = TestBed.createComponent(CustomizableDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
