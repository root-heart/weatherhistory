import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PrecipitationChart} from './precipitation-chart.component';

describe('PrecipitationChartComponent', () => {
  let component: PrecipitationChart;
  let fixture: ComponentFixture<PrecipitationChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PrecipitationChart ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PrecipitationChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
