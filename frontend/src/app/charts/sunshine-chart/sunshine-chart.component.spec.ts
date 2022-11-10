import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SunshineChart } from './sunshine-chart.component';

describe('SunshineChartComponent', () => {
  let component: SunshineChart;
  let fixture: ComponentFixture<SunshineChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SunshineChart ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SunshineChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
