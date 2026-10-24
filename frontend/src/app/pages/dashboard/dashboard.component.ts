import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { BankService } from '../../core/bank.service';
import { Profile, Transaction } from '../../core/models';

type Op = 'transfer' | 'deposit' | 'withdraw';
type Filter = 'all' | 'DEPOSIT' | 'WITHDRAWAL' | 'sent' | 'received';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="vb-shell" style="max-width:1120px;margin:0 auto;padding:24px 20px 60px;">
      <header style="display:flex;align-items:center;justify-content:space-between;margin-bottom:24px;">
        <div class="vb-brand">Valencia<span>Bank</span></div>
        <button class="vb-btn vb-btn-ghost" style="width:auto;padding:8px 16px;" (click)="logout()">Salir</button>
      </header>

      <div class="vb-grid">

        <!-- ========= IZQUIERDA ========= -->
        <div class="vb-col">
          <!-- 1. Perfil -->
          <div class="vb-card" style="text-align:center;">
            @if (avatar()) {
              <img class="vb-avatar" [src]="avatar()" alt="avatar" style="margin:0 auto;" />
            } @else {
              <div class="vb-avatar" style="margin:0 auto;">{{ initials() }}</div>
            }
            <div style="font-weight:600;margin-top:12px;font-size:1.05rem;">{{ displayName() }}</div>
            <div class="vb-muted" style="font-size:0.82rem;margin-top:2px;">
              {{ profile()?.locality || 'Localidad sin especificar' }}
            </div>
            <label class="vb-upload">
              Cambiar foto
              <input type="file" accept="image/*" (change)="onAvatar($event)" />
            </label>
          </div>

          <!-- 2. Cuenta -->
          <div class="vb-card">
            <h3 style="margin:0 0 12px;font-size:0.95rem;font-weight:600;">Mi cuenta</h3>
            <div class="vb-stat"><span class="vb-secondary">Email</span> <b style="font-size:0.8rem;">{{ profile()?.email }}</b></div>
            <div class="vb-stat"><span class="vb-secondary">IBAN</span></div>
            <div style="font-family:monospace;font-size:0.78rem;color:var(--accent-soft);word-break:break-all;">
              {{ profile()?.accountNumber }}
            </div>
          </div>

          <!-- 3. Resumen y filtros -->
          <div class="vb-card">
            <h3 style="margin:0 0 10px;font-size:0.95rem;font-weight:600;">Resumen</h3>
            <div class="vb-stat"><span class="vb-secondary">Ingresos</span> <b>{{ counts().deposits }}</b></div>
            <div class="vb-stat"><span class="vb-secondary">Retiros</span> <b>{{ counts().withdrawals }}</b></div>
            <div class="vb-stat"><span class="vb-secondary">Transferencias</span> <b>{{ counts().transfers }}</b></div>
            <div class="vb-stat"><span class="vb-secondary">Total movimientos</span> <b>{{ transactions().length }}</b></div>
            <p class="vb-secondary" style="font-size:0.78rem;margin:14px 0 8px;">Filtrar historial</p>
            <div class="vb-chips">
              <span class="vb-chip" [class.active]="filter()==='all'" (click)="filter.set('all')">Todos</span>
              <span class="vb-chip" [class.active]="filter()==='DEPOSIT'" (click)="filter.set('DEPOSIT')">Ingresos</span>
              <span class="vb-chip" [class.active]="filter()==='WITHDRAWAL'" (click)="filter.set('WITHDRAWAL')">Retiros</span>
              <span class="vb-chip" [class.active]="filter()==='sent'" (click)="filter.set('sent')">Enviadas</span>
              <span class="vb-chip" [class.active]="filter()==='received'" (click)="filter.set('received')">Recibidas</span>
            </div>
          </div>
        </div>

        <!-- ========= CENTRO ========= -->
        <div class="vb-col">
          <div class="vb-card">
            <p class="vb-secondary" style="margin:0 0 8px;font-size:0.8rem;">Saldo disponible</p>
            <div style="font-size:2.6rem;font-weight:700;letter-spacing:-0.02em;">
              {{ (profile()?.balance ?? 0) | number:'1.2-2' }} <span style="font-size:1.5rem;">&euro;</span>
            </div>
          </div>

          <div class="vb-card">
            <h3 style="margin:0 0 14px;font-size:1rem;font-weight:600;">Movimientos</h3>
            @if (filtered().length === 0) {
              <p class="vb-muted" style="font-size:0.9rem;">No hay movimientos para este filtro.</p>
            }
            @for (t of filtered(); track t.id) {
              <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--border-soft);">
                <div>
                  <div style="font-weight:500;font-size:0.9rem;">{{ label(t) }}</div>
                  <div class="vb-muted" style="font-size:0.74rem;font-family:monospace;">{{ counterparty(t) }}</div>
                </div>
                <div style="text-align:right;">
                  <div style="font-weight:600;font-size:0.95rem;" [style.color]="dir(t)==='in' ? 'var(--green)' : 'var(--red)'">
                    {{ dir(t)==='in' ? '+' : '-' }}{{ t.amount | number:'1.2-2' }} &euro;
                  </div>
                  <div class="vb-muted" style="font-size:0.72rem;">{{ t.createdAt | date:'dd/MM/yy HH:mm' }}</div>
                </div>
              </div>
            }
          </div>
        </div>

        <!-- ========= DERECHA ========= -->
        <div class="vb-col">
          <div class="vb-card">
            <h3 style="margin:0 0 14px;font-size:1rem;font-weight:600;">Operaciones</h3>
            <div class="vb-seg">
              <button [class.active]="op()==='transfer'" (click)="setOp('transfer')">Transferir</button>
              <button [class.active]="op()==='deposit'" (click)="setOp('deposit')">Ingresar</button>
              <button [class.active]="op()==='withdraw'" (click)="setOp('withdraw')">Retirar</button>
            </div>

            @if (op()==='transfer') {
              <label class="vb-label">Cuenta destino</label>
              <input class="vb-input" [(ngModel)]="toAccount" placeholder="ES..." style="margin-bottom:14px;" />
            }

            <label class="vb-label">Importe (&euro;)</label>
            <input class="vb-input" type="number" [(ngModel)]="amount" placeholder="0.00" min="0.01" step="0.01" style="margin-bottom:18px;" />

            <button class="vb-btn" (click)="submit()" [disabled]="loading()">
              {{ loading() ? 'Procesando...' : btnLabel() }}
            </button>
            @if (error()) { <p class="vb-error">{{ error() }}</p> }
            @if (ok()) { <p class="vb-error vb-ok">{{ ok() }}</p> }
          </div>
        </div>
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  private bank = inject(BankService);
  private auth = inject(AuthService);
  private router = inject(Router);

  profile = signal<Profile | null>(null);
  transactions = signal<Transaction[]>([]);
  avatar = signal<string | null>(null);

  op = signal<Op>('transfer');
  filter = signal<Filter>('all');

  toAccount = '';
  amount: number | null = null;
  error = signal<string | null>(null);
  ok = signal<string | null>(null);
  loading = signal(false);

  counts = computed(() => {
    const txs = this.transactions();
    return {
      deposits: txs.filter(t => t.type === 'DEPOSIT').length,
      withdrawals: txs.filter(t => t.type === 'WITHDRAWAL').length,
      transfers: txs.filter(t => t.type === 'TRANSFER').length,
    };
  });

  filtered = computed(() => {
    const acct = this.profile()?.accountNumber;
    const f = this.filter();
    return this.transactions().filter(t => {
      if (f === 'all') return true;
      if (f === 'DEPOSIT') return t.type === 'DEPOSIT';
      if (f === 'WITHDRAWAL') return t.type === 'WITHDRAWAL';
      if (f === 'sent') return t.type === 'TRANSFER' && t.fromAccountNumber === acct;
      if (f === 'received') return t.type === 'TRANSFER' && t.toAccountNumber === acct;
      return true;
    });
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.bank.getProfile().subscribe({
      next: p => {
        this.profile.set(p);
        this.loadAvatar();
      },
    });
    this.bank.getTransactions().subscribe({ next: page => this.transactions.set(page.content) });
  }

  setOp(o: Op): void {
    this.op.set(o);
    this.error.set(null);
    this.ok.set(null);
  }

  btnLabel(): string {
    return this.op() === 'transfer' ? 'Enviar dinero' : this.op() === 'deposit' ? 'Ingresar' : 'Retirar';
  }

  submit(): void {
    this.error.set(null);
    this.ok.set(null);
    if (!this.amount || this.amount <= 0) {
      this.error.set('Indica un importe valido');
      return;
    }
    if (this.op() === 'transfer' && !this.toAccount) {
      this.error.set('Indica la cuenta destino');
      return;
    }
    this.loading.set(true);
    const done = (msg: string) => {
      this.loading.set(false);
      this.ok.set(msg);
      this.toAccount = '';
      this.amount = null;
      this.reload();
    };
    const fail = (e: { error?: { message?: string; error?: string } }) => {
      this.loading.set(false);
      this.error.set(e?.error?.message || e?.error?.error || 'Operacion fallida');
    };

    if (this.op() === 'transfer') {
      this.bank.transfer(this.toAccount, this.amount).subscribe({ next: () => done('Transferencia realizada'), error: fail });
    } else if (this.op() === 'deposit') {
      this.bank.deposit(this.amount).subscribe({ next: () => done('Ingreso realizado'), error: fail });
    } else {
      this.bank.withdraw(this.amount).subscribe({ next: () => done('Retiro realizado'), error: fail });
    }
  }

  // --- avatar (se guarda en el navegador, demo) ---
  onAvatar(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      const url = reader.result as string;
      this.avatar.set(url);
      try { localStorage.setItem(this.avatarKey(), url); } catch { /* modo privado */ }
    };
    reader.readAsDataURL(file);
  }

  private loadAvatar(): void {
    try {
      const saved = localStorage.getItem(this.avatarKey());
      if (saved) this.avatar.set(saved);
    } catch { /* ignore */ }
  }

  private avatarKey(): string {
    return 'vb_avatar_' + (this.profile()?.email ?? 'x');
  }

  // --- presentacion de movimientos ---
  dir(t: Transaction): 'in' | 'out' {
    if (t.type === 'DEPOSIT') return 'in';
    if (t.type === 'WITHDRAWAL') return 'out';
    return t.toAccountNumber === this.profile()?.accountNumber ? 'in' : 'out';
  }

  label(t: Transaction): string {
    if (t.type === 'DEPOSIT') return 'Ingreso';
    if (t.type === 'WITHDRAWAL') return 'Retiro';
    return this.dir(t) === 'in' ? 'Recibida' : 'Enviada';
  }

  counterparty(t: Transaction): string {
    if (t.type === 'DEPOSIT') return 'Ingreso de efectivo';
    if (t.type === 'WITHDRAWAL') return 'Retiro de efectivo';
    return this.dir(t) === 'in' ? (t.fromAccountNumber ?? 'Banco') : (t.toAccountNumber ?? '-');
  }

  displayName(): string {
    return this.profile()?.fullName || this.profile()?.email || 'Usuario';
  }

  initials(): string {
    const name = this.profile()?.fullName || this.profile()?.email || '?';
    const parts = name.trim().split(/\s+/);
    return (parts.length > 1 ? parts[0][0] + parts[1][0] : name.slice(0, 2)).toUpperCase();
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
