import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CloudinessChartComponent } from './cloudiness-chart.component';

describe('CloudinessChartComponent', () => {
  let component: CloudinessChartComponent;
  let fixture: ComponentFixture<CloudinessChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CloudinessChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CloudinessChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
