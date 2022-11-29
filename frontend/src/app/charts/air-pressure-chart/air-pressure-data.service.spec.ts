import {TestBed} from '@angular/core/testing';

import {AirPressureDataService} from './air-pressure-data.service';

describe('AirPressureDataService', () => {
  let service: AirPressureDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AirPressureDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
