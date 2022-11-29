import {TestBed} from '@angular/core/testing';

import {WindSpeedDataService} from './wind-speed-data.service';

describe('WindSpeedDataService', () => {
  let service: WindSpeedDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WindSpeedDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
