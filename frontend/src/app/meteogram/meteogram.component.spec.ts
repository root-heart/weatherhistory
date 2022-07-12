import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Meteogram } from './meteogram.component';

describe('MeteogramComponent', () => {
  let component: Meteogram;
  let fixture: ComponentFixture<Meteogram>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Meteogram ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Meteogram);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
