import {TestBed} from '@angular/core/testing';

import {PrecipitationDataService} from './precipitation-data.service';

describe('PrecipitationDataService', () => {
  let service: PrecipitationDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrecipitationDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
