import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StationChartsComponent } from './station-charts.component';

describe('StationChartsComponent', () => {
  let component: StationChartsComponent;
  let fixture: ComponentFixture<StationChartsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StationChartsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StationChartsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
