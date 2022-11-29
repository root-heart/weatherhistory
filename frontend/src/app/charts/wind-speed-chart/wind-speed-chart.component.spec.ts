import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WindSpeedChart} from './wind-speed-chart.component';

describe('WindSpeedChartComponent', () => {
  let component: WindSpeedChart;
  let fixture: ComponentFixture<WindSpeedChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WindSpeedChart ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WindSpeedChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
