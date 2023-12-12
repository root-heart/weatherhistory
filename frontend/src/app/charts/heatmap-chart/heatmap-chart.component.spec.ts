import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeatmapChart } from './heatmap-chart.component';

describe('HeatmapChartComponent', () => {
  let component: HeatmapChart;
  let fixture: ComponentFixture<HeatmapChart>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [HeatmapChart]
    });
    fixture = TestBed.createComponent(HeatmapChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
