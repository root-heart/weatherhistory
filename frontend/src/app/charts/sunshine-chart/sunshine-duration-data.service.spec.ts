import {TestBed} from '@angular/core/testing';

import {SunshineDurationDataService} from './sunshine-duration-data.service';

describe('SunshineDurationDataService', () => {
  let service: SunshineDurationDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SunshineDurationDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
