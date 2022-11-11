import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemperatureChart } from './temperature-chart.component';

describe('TemperatureChartComponent', () => {
  let component: TemperatureChart;
  let fixture: ComponentFixture<TemperatureChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemperatureChart ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemperatureChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
