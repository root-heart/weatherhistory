import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StationAndDateFilterComponent } from './station-and-date-filter.component';

describe('FilterHeaderComponent', () => {
  let component: StationAndDateFilterComponent;
  let fixture: ComponentFixture<StationAndDateFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StationAndDateFilterComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StationAndDateFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
