export const API_URL = 'http://localhost:8080';

export interface Account {
  accountNumber: string;
  balance: number;
}

export interface Transaction {
  id: number;
  fromAccountNumber: string | null;
  toAccountNumber: string;
  amount: number;
  type: string;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}
