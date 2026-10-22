import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Account, API_URL, Page, Transaction } from './models';

@Injectable({ providedIn: 'root' })
export class BankService {
  constructor(private http: HttpClient) {}

  getAccount(): Observable<Account> {
    return this.http.get<Account>(`${API_URL}/accounts/me`);
  }

  getTransactions(): Observable<Page<Transaction>> {
    return this.http.get<Page<Transaction>>(`${API_URL}/accounts/me/transactions`);
  }

  transfer(toAccountNumber: string, amount: number): Observable<unknown> {
    return this.http.post(`${API_URL}/transfers`, { toAccountNumber, amount });
  }
}
