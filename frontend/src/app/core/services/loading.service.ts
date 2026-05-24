import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private activeRequests = 0;
  readonly loading = signal(false);

  start(): void {
    this.activeRequests += 1;
    this.loading.set(true);
  }

  stop(): void {
    this.activeRequests = Math.max(0, this.activeRequests - 1);
    this.loading.set(this.activeRequests > 0);
  }
}
