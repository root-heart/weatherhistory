import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SunshineCloudCoverageHeatmapChartComponent } from './sunshine-cloud-coverage-heatmap-chart.component';

describe('SunshineCloudCoverageHeatmapChartComponent', () => {
  let component: SunshineCloudCoverageHeatmapChartComponent;
  let fixture: ComponentFixture<SunshineCloudCoverageHeatmapChartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SunshineCloudCoverageHeatmapChartComponent]
    });
    fixture = TestBed.createComponent(SunshineCloudCoverageHeatmapChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
