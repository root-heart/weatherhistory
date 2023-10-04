import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TabView } from './tab-view.component';

describe('ChartTabGroupComponent', () => {
  let component: TabView;
  let fixture: ComponentFixture<TabView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TabView ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TabView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
