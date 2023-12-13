import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WeatherStationMap } from './weather-station-map.component';

describe('FilterHeaderComponent', () => {
  let component: WeatherStationMap;
  let fixture: ComponentFixture<WeatherStationMap>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WeatherStationMap ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WeatherStationMap);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
