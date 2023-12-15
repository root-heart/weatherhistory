import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DropdownList } from './dropdown-list.component';

describe('DropdownComponent', () => {
  let component: DropdownList;
  let fixture: ComponentFixture<DropdownList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DropdownList ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DropdownList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
