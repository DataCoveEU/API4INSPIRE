import { TestBed } from '@angular/core/testing';

import { SqlService } from './sql.service';

describe('SqlService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SqlService = TestBed.get(SqlService);
    expect(service).toBeTruthy();
  });
});
