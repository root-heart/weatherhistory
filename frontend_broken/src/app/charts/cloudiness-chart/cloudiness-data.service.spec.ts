import {TestBed} from '@angular/core/testing';

import {CloudinessDataService} from './cloudiness-data.service';

describe('CloudinessDataService', () => {
  let service: CloudinessDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CloudinessDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
