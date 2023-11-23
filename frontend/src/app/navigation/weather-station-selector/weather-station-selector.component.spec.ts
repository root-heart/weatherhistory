import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WeatherStationSelectorComponent } from './weather-station-selector.component';

describe('WeatherStationSelectorComponent', () => {
  let component: WeatherStationSelectorComponent;
  let fixture: ComponentFixture<WeatherStationSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WeatherStationSelectorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WeatherStationSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
