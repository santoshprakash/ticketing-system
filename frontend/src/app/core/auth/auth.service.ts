import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiService } from '../services/api.service';
import { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../models/auth.models';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<AuthUser | null>(this.tokenStorage.user);

  constructor(
    private readonly api: ApiService,
    private readonly tokenStorage: TokenStorageService,
    private readonly router: Router
  ) {}

  get isAuthenticated(): boolean {
    return Boolean(this.tokenStorage.accessToken);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/login', request).pipe(tap((response) => this.applyAuth(response)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/register', request).pipe(tap((response) => this.applyAuth(response)));
  }

  forgotPassword(email: string): Observable<{ message: string }> {
    return this.api.post<{ message: string }>('/auth/forgot-password', { email });
  }

  resetPassword(token: string, newPassword: string): Observable<{ message: string }> {
    return this.api.post<{ message: string }>('/auth/reset-password', { token, newPassword });
  }

  changePassword(currentPassword: string, newPassword: string): Observable<{ message: string }> {
    return this.api.post<{ message: string }>('/auth/change-password', { currentPassword, newPassword });
  }

  logout(): void {
    this.currentUser.set(null);
    this.tokenStorage.clear();
    void this.router.navigate(['/auth/login']);
  }

  private applyAuth(response: AuthResponse): void {
    this.tokenStorage.setTokens(response.accessToken, response.refreshToken, response.user);
    this.currentUser.set(response.user);
  }
}
