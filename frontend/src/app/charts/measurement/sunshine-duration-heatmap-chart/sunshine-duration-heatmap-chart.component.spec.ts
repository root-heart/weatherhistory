import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SunshineDurationHeatmapChartComponent } from './sunshine-duration-heatmap-chart.component';

describe('SunshineDurationHeatmapChartComponent', () => {
  let component: SunshineDurationHeatmapChartComponent;
  let fixture: ComponentFixture<SunshineDurationHeatmapChartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SunshineDurationHeatmapChartComponent]
    });
    fixture = TestBed.createComponent(SunshineDurationHeatmapChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
