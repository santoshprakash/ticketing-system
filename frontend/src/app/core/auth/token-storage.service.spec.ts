import { TestBed } from '@angular/core/testing';
import { TokenStorageService } from './token-storage.service';

describe('TokenStorageService', () => {
  let service: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenStorageService);
  });

  afterEach(() => localStorage.clear());

  it('persists tokens and the authenticated user', () => {
    service.setTokens('access-token', 'refresh-token', {
      id: 'user-1',
      email: 'admin@ticketing.local',
      fullName: 'Admin User',
      role: 'ADMIN'
    });

    expect(service.accessToken).toBe('access-token');
    expect(service.refreshToken).toBe('refresh-token');
    expect(service.user?.role).toBe('ADMIN');
  });
});
