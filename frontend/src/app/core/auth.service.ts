import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_URL } from './models';

const TOKEN_KEY = 'vb_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // signal con el token; se inicializa desde localStorage para no perder la sesion al recargar.
  private token = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  constructor(private http: HttpClient) {}

  register(email: string, password: string): Observable<unknown> {
    return this.http.post(`${API_URL}/auth/register`, { email, password });
  }

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http
      .post<{ token: string }>(`${API_URL}/auth/login`, { email, password })
      .pipe(tap(res => this.setToken(res.token)));
  }

  getToken(): string | null {
    return this.token();
  }

  isLoggedIn(): boolean {
    return this.token() !== null;
  }

  logout(): void {
    this.token.set(null);
    localStorage.removeItem(TOKEN_KEY);
  }

  private setToken(value: string): void {
    this.token.set(value);
    localStorage.setItem(TOKEN_KEY, value);
  }
}
