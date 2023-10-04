import { ComponentFixture, TestBed } from '@angular/core/testing';

import { YearSelectionComponent } from './year-selection.component';

describe('YearSelectionComponent', () => {
  let component: YearSelectionComponent;
  let fixture: ComponentFixture<YearSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ YearSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(YearSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
