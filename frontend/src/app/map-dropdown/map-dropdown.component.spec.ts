import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapDropdown } from './map-dropdown.component';

describe('FilterHeaderComponent', () => {
  let component: MapDropdown;
  let fixture: ComponentFixture<MapDropdown>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MapDropdown ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MapDropdown);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
