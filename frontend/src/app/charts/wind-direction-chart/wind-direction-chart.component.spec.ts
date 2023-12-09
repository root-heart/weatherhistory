import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WindDirectionChart } from './wind-direction-chart.component';

describe('WindDirectionChartComponent', () => {
  let component: WindDirectionChart;
  let fixture: ComponentFixture<WindDirectionChart>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WindDirectionChart]
    });
    fixture = TestBed.createComponent(WindDirectionChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
