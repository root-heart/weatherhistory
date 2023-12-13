import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChartTileComponent } from './chart-tile.component';

describe('ChartTileComponent', () => {
  let component: ChartTileComponent;
  let fixture: ComponentFixture<ChartTileComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ChartTileComponent]
    });
    fixture = TestBed.createComponent(ChartTileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
