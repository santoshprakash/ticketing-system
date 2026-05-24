import { DOCUMENT } from '@angular/common';
import { Inject, Injectable, signal } from '@angular/core';

export type ThemeMode = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly storageKey = 'ticketing.theme';
  readonly theme = signal<ThemeMode>((localStorage.getItem(this.storageKey) as ThemeMode) || 'light');

  constructor(@Inject(DOCUMENT) private readonly document: Document) {
    this.apply(this.theme());
  }

  toggle(): void {
    this.setTheme(this.theme() === 'light' ? 'dark' : 'light');
  }

  setTheme(theme: ThemeMode): void {
    localStorage.setItem(this.storageKey, theme);
    this.theme.set(theme);
    this.apply(theme);
  }

  private apply(theme: ThemeMode): void {
    this.document.documentElement.dataset['theme'] = theme;
  }
}
