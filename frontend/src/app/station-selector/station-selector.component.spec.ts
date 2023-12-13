import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StationSelectorComponent } from './station-selector.component';

describe('StationSelectorComponent', () => {
  let component: StationSelectorComponent;
  let fixture: ComponentFixture<StationSelectorComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StationSelectorComponent]
    });
    fixture = TestBed.createComponent(StationSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
