import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthUser } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly userStorageKey = 'ticketing.auth.user';

  get accessToken(): string | null {
    return localStorage.getItem(environment.tokenStorageKey);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(environment.refreshTokenStorageKey);
  }

  get user(): AuthUser | null {
    const value = localStorage.getItem(this.userStorageKey);
    return value ? (JSON.parse(value) as AuthUser) : null;
  }

  setTokens(accessToken: string, refreshToken: string, user?: AuthUser): void {
    localStorage.setItem(environment.tokenStorageKey, accessToken);
    localStorage.setItem(environment.refreshTokenStorageKey, refreshToken);
    if (user) {
      localStorage.setItem(this.userStorageKey, JSON.stringify(user));
    }
  }

  clear(): void {
    localStorage.removeItem(environment.tokenStorageKey);
    localStorage.removeItem(environment.refreshTokenStorageKey);
    localStorage.removeItem(this.userStorageKey);
  }
}
