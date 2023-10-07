import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MinAvgMaxChart } from './min-avg-max-chart.component';

describe('ChartToBeRenamedComponent', () => {
  let component: MinAvgMaxChart;
  let fixture: ComponentFixture<MinAvgMaxChart>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MinAvgMaxChart]
    });
    fixture = TestBed.createComponent(MinAvgMaxChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
