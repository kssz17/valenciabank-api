import { Injectable, signal } from '@angular/core';

const KEY = 'vb_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  theme = signal<'dark' | 'light'>('dark');

  init(): void {
    let saved: string | null = null;
    try { saved = localStorage.getItem(KEY); } catch { /* modo privado */ }
    this.apply(saved === 'light' ? 'light' : 'dark');
  }

  toggle(): void {
    this.apply(this.theme() === 'dark' ? 'light' : 'dark');
  }

  private apply(t: 'dark' | 'light'): void {
    this.theme.set(t);
    document.documentElement.setAttribute('data-theme', t);
    try { localStorage.setItem(KEY, t); } catch { /* modo privado */ }
  }
}
