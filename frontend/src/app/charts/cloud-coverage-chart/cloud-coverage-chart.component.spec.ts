import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CloudCoverageChart } from './cloud-coverage-chart.component';

describe('CloudCoverageChartComponent', () => {
  let component: CloudCoverageChart;
  let fixture: ComponentFixture<CloudCoverageChart>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CloudCoverageChart]
    });
    fixture = TestBed.createComponent(CloudCoverageChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
