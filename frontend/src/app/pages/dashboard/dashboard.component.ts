import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { BankService } from '../../core/bank.service';
import { Account, Transaction } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="vb-shell" style="max-width:720px;margin:0 auto;padding:24px 20px 60px;">
      <header style="display:flex;align-items:center;justify-content:space-between;margin-bottom:28px;">
        <div class="vb-brand">Valencia<span>Bank</span></div>
        <button class="vb-btn vb-btn-ghost" style="width:auto;padding:8px 16px;" (click)="logout()">Salir</button>
      </header>

      <!-- Saldo -->
      <div class="vb-card" style="margin-bottom:20px;">
        <p class="vb-secondary" style="margin:0 0 8px;font-size:0.8rem;">Saldo disponible</p>
        <div style="font-size:2.4rem;font-weight:700;letter-spacing:-0.02em;">
          {{ (account()?.balance ?? 0) | number:'1.2-2' }} <span style="font-size:1.4rem;">€</span>
        </div>
        <p class="vb-muted" style="margin:8px 0 0;font-size:0.8rem;font-family:monospace;">
          {{ account()?.accountNumber }}
        </p>
      </div>

      <!-- Transferir -->
      <div class="vb-card" style="margin-bottom:20px;">
        <h3 style="margin:0 0 16px;font-size:1rem;font-weight:600;">Transferir</h3>
        <label class="vb-label">Cuenta destino</label>
        <input class="vb-input" [(ngModel)]="toAccount" placeholder="ES..." style="margin-bottom:14px;" />
        <label class="vb-label">Importe (€)</label>
        <input class="vb-input" type="number" [(ngModel)]="amount" placeholder="0.00" min="0.01" step="0.01" style="margin-bottom:18px;" />
        <button class="vb-btn" (click)="transfer()" [disabled]="loading()">
          {{ loading() ? 'Enviando...' : 'Enviar dinero' }}
        </button>
        @if (error()) { <p class="vb-error">{{ error() }}</p> }
        @if (ok()) { <p class="vb-error vb-ok">{{ ok() }}</p> }
      </div>

      <!-- Historial -->
      <div class="vb-card">
        <h3 style="margin:0 0 16px;font-size:1rem;font-weight:600;">Movimientos</h3>
        @if (transactions().length === 0) {
          <p class="vb-muted" style="font-size:0.9rem;">Aún no hay movimientos.</p>
        }
        @for (t of transactions(); track t.id) {
          <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--border-soft);">
            <div>
              <div style="font-weight:500;font-size:0.9rem;">
                {{ t.type === 'DEPOSIT' ? 'Ingreso' : (isIncoming(t) ? 'Recibida' : 'Enviada') }}
              </div>
              <div class="vb-muted" style="font-size:0.75rem;font-family:monospace;">
                {{ isIncoming(t) ? (t.fromAccountNumber || 'Banco') : t.toAccountNumber }}
              </div>
            </div>
            <div style="text-align:right;">
              <div style="font-weight:600;font-size:0.95rem;" [style.color]="isIncoming(t) ? 'var(--green)' : 'var(--red)'">
                {{ isIncoming(t) ? '+' : '-' }}{{ t.amount | number:'1.2-2' }} €
              </div>
              <div class="vb-muted" style="font-size:0.72rem;">{{ t.createdAt | date:'dd/MM/yy HH:mm' }}</div>
            </div>
          </div>
        }
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  private bank = inject(BankService);
  private auth = inject(AuthService);
  private router = inject(Router);

  account = signal<Account | null>(null);
  transactions = signal<Transaction[]>([]);
  toAccount = '';
  amount: number | null = null;
  error = signal<string | null>(null);
  ok = signal<string | null>(null);
  loading = signal(false);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.bank.getAccount().subscribe({ next: a => this.account.set(a) });
    this.bank.getTransactions().subscribe({ next: p => this.transactions.set(p.content) });
  }

  transfer(): void {
    this.error.set(null);
    this.ok.set(null);
    if (!this.toAccount || !this.amount || this.amount <= 0) {
      this.error.set('Indica una cuenta destino y un importe válido');
      return;
    }
    this.loading.set(true);
    this.bank.transfer(this.toAccount, this.amount).subscribe({
      next: () => {
        this.loading.set(false);
        this.ok.set('Transferencia realizada');
        this.toAccount = '';
        this.amount = null;
        this.reload();
      },
      error: e => {
        this.loading.set(false);
        this.error.set(e?.error?.message || e?.error?.error || 'Error en la transferencia');
      },
    });
  }

  isIncoming(t: Transaction): boolean {
    return t.toAccountNumber === this.account()?.accountNumber;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
