import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DateRangeSelector } from './date-range-selector.component';

describe('DateRangeDropdownComponent', () => {
  let component: DateRangeSelector;
  let fixture: ComponentFixture<DateRangeSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DateRangeSelector ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DateRangeSelector);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
