import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SumChartComponent } from './sum-chart.component';

describe('SumChartComponent', () => {
  let component: SumChartComponent;
  let fixture: ComponentFixture<SumChartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SumChartComponent]
    });
    fixture = TestBed.createComponent(SumChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
