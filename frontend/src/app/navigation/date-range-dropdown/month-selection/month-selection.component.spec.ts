import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthSelectionComponent } from './month-selection.component';

describe('MonthSelectionComponent', () => {
  let component: MonthSelectionComponent;
  let fixture: ComponentFixture<MonthSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MonthSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonthSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
