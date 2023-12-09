import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CloudCoverageChartComponent } from './cloud-coverage-chart.component';

describe('CloudCoverageChartComponent', () => {
  let component: CloudCoverageChartComponent;
  let fixture: ComponentFixture<CloudCoverageChartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CloudCoverageChartComponent]
    });
    fixture = TestBed.createComponent(CloudCoverageChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
