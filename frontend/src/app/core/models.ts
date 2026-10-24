export const API_URL = 'http://localhost:8080';

export interface Profile {
  email: string;
  fullName: string | null;
  locality: string | null;
  accountNumber: string;
  balance: number;
}

export interface Account {
  accountNumber: string;
  balance: number;
}

export interface Transaction {
  id: number;
  fromAccountNumber: string | null;
  toAccountNumber: string | null;
  amount: number;
  type: string;
  category: string | null;
  description: string | null;
  createdAt: string;
}

export interface CategorySummary {
  category: string;
  total: number;
  count: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}
