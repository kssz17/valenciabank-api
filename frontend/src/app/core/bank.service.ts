import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Account, API_URL, CategorySummary, Page, Profile, Transaction } from './models';

@Injectable({ providedIn: 'root' })
export class BankService {
  constructor(private http: HttpClient) {}

  getProfile(): Observable<Profile> {
    return this.http.get<Profile>(`${API_URL}/accounts/me`);
  }

  getTransactions(): Observable<Page<Transaction>> {
    return this.http.get<Page<Transaction>>(`${API_URL}/accounts/me/transactions`);
  }

  getSummary(): Observable<CategorySummary[]> {
    return this.http.get<CategorySummary[]>(`${API_URL}/accounts/me/summary`);
  }

  transfer(toAccountNumber: string, amount: number, description?: string): Observable<unknown> {
    return this.http.post(`${API_URL}/transfers`, { toAccountNumber, amount, description });
  }

  deposit(amount: number, category?: string, description?: string): Observable<Account> {
    return this.http.post<Account>(`${API_URL}/accounts/me/deposit`, { amount, category, description });
  }

  withdraw(amount: number, category?: string, description?: string): Observable<Account> {
    return this.http.post<Account>(`${API_URL}/accounts/me/withdraw`, { amount, category, description });
  }
}
