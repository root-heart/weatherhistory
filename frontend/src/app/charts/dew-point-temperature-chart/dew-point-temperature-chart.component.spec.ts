import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DewPointTemperatureChart } from './dew-point-temperature-chart.component';

describe('DewPointTemperatureChartComponent', () => {
  let component: DewPointTemperatureChart;
  let fixture: ComponentFixture<DewPointTemperatureChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DewPointTemperatureChart ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DewPointTemperatureChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
