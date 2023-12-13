import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AirTemperatureHeatmapChartComponent } from './air-temperature-heatmap-chart.component';

describe('AirTemperatureHeatmapChartComponent', () => {
  let component: AirTemperatureHeatmapChartComponent;
  let fixture: ComponentFixture<AirTemperatureHeatmapChartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AirTemperatureHeatmapChartComponent]
    });
    fixture = TestBed.createComponent(AirTemperatureHeatmapChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
