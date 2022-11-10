import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AirPressureChart } from './air-pressure-chart.component';

describe('AirPressureChartComponent', () => {
  let component: AirPressureChart;
  let fixture: ComponentFixture<AirPressureChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AirPressureChart ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AirPressureChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
