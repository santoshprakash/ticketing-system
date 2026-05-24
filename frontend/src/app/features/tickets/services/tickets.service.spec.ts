import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { TicketsService } from './tickets.service';

describe('TicketsService', () => {
  let service: TicketsService;
  let api: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    api = jasmine.createSpyObj<ApiService>('ApiService', ['post']);

    TestBed.configureTestingModule({
      providers: [
        TicketsService,
        { provide: ApiService, useValue: api }
      ]
    });

    service = TestBed.inject(TicketsService);
  });

  it('uses POST for ticket search endpoints', () => {
    api.post.and.returnValue(of({ content: [], number: 0, size: 10, totalElements: 0, totalPages: 0 }));

    service.search({ status: 'OPEN' }, 0, 10).subscribe();

    expect(api.post).toHaveBeenCalledWith('/tickets/search', {}, jasmine.objectContaining({ status: 'OPEN', page: 0, size: 10 }));
  });

  it('creates tickets through the API contract', () => {
    const request = { title: 'Cannot access portal', description: 'Customer cannot access the portal after password reset.', priority: 'HIGH' as const, category: 'ACCESS' };
    api.post.and.returnValue(of({ id: '1' }));

    service.create(request).subscribe();

    expect(api.post).toHaveBeenCalledWith('/tickets', request);
  });
});
