import { TestBed } from '@angular/core/testing';

import { FetchMeasurementsService } from './fetch-measurements.service';

describe('FetchMeasurementsServiceService', () => {
  let service: FetchMeasurementsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FetchMeasurementsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
