import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ToggableButtonComponent } from './toggable-button.component';

describe('ToggableButtonComponent', () => {
  let component: ToggableButtonComponent;
  let fixture: ComponentFixture<ToggableButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ToggableButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ToggableButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
