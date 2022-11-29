import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CloudinessChart} from './cloudiness-chart.component';

describe('CloudinessChartComponent', () => {
  let component: CloudinessChart;
  let fixture: ComponentFixture<CloudinessChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CloudinessChart ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CloudinessChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
