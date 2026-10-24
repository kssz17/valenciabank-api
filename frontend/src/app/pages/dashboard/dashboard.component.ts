import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { BankService } from '../../core/bank.service';
import { ThemeService } from '../../core/theme.service';
import { CategorySummary, Profile, Transaction } from '../../core/models';

type Op = 'transfer' | 'deposit' | 'withdraw';
type Filter = 'all' | 'DEPOSIT' | 'WITHDRAWAL' | 'sent' | 'received';

interface Cat { key: string; label: string; icon: string; }

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="vb-shell" style="max-width:1120px;margin:0 auto;padding:24px 20px 60px;">
      <header style="display:flex;align-items:center;justify-content:space-between;margin-bottom:24px;">
        <div class="vb-brand">Valencia<span>Bank</span></div>
        <div style="display:flex;gap:10px;">
          <button class="vb-btn vb-btn-ghost" style="width:auto;padding:8px 12px;" (click)="theme.toggle()" aria-label="Cambiar tema">
            <i class="ti" [class.ti-moon]="theme.theme()==='light'" [class.ti-sun]="theme.theme()==='dark'" style="font-size:16px;vertical-align:-2px;"></i>
          </button>
          <button class="vb-btn vb-btn-ghost" style="width:auto;padding:8px 16px;" (click)="logout()">Salir</button>
        </div>
      </header>

      <div class="vb-grid">

        <!-- ========= IZQUIERDA ========= -->
        <div class="vb-col">
          <!-- Perfil -->
          <div class="vb-card" style="text-align:center;">
            @if (avatar()) {
              <img class="vb-avatar" [src]="avatar()" alt="avatar" style="margin:0 auto;" />
            } @else {
              <div class="vb-avatar" style="margin:0 auto;">{{ initials() }}</div>
            }
            <div style="font-weight:600;margin-top:12px;font-size:1.05rem;">{{ displayName() }}</div>
            <div class="vb-muted" style="font-size:0.82rem;margin-top:2px;">
              <i class="ti ti-map-pin" style="font-size:14px;vertical-align:-2px;"></i>
              {{ profile()?.locality || 'Sin localidad' }}
            </div>
            <label class="vb-upload"><i class="ti ti-camera" style="font-size:14px;vertical-align:-2px;"></i> Cambiar foto
              <input type="file" accept="image/*" (change)="onAvatar($event)" />
            </label>
          </div>

          <!-- Tarjeta virtual -->
          <div class="vb-vcard">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
              <span style="font-size:0.72rem;color:#cbb88a;">Tarjeta ValenciaBank</span>
              <i class="ti ti-wifi" style="font-size:16px;color:#ffd659;transform:rotate(90deg);"></i>
            </div>
            <div style="width:34px;height:24px;border-radius:5px;background:#ffbb00;margin:6px 0;"></div>
            <div style="font-family:monospace;font-size:0.92rem;letter-spacing:2px;">{{ cardNumber() }}</div>
            <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-top:10px;">
              <span style="font-size:0.78rem;">{{ displayName() }}</span>
              <span style="font-size:0.9rem;font-weight:600;color:#ffd659;">{{ (profile()?.balance ?? 0) | number:'1.2-2' }} &euro;</span>
            </div>
          </div>

          <!-- Gastos por categoria -->
          <div class="vb-card">
            <h3 style="margin:0 0 12px;font-size:0.95rem;font-weight:600;">Gastos por categoria</h3>
            @if (summary().length === 0) {
              <p class="vb-muted" style="font-size:0.82rem;">Aun no has registrado gastos. Usa <b>Retirar</b> para anotar uno.</p>
            }
            @for (s of summary(); track s.category) {
              <div style="margin-bottom:10px;">
                <div style="display:flex;justify-content:space-between;font-size:0.82rem;margin-bottom:4px;">
                  <span><i class="ti {{ iconFor(s.category) }}" style="font-size:15px;vertical-align:-2px;color:var(--accent);"></i> {{ labelFor(s.category) }}</span>
                  <b>{{ s.total | number:'1.2-2' }} &euro;</b>
                </div>
                <div style="height:6px;background:var(--track);border-radius:999px;overflow:hidden;">
                  <div style="height:100%;background:var(--gradient);" [style.width.%]="barPct(s)"></div>
                </div>
              </div>
            }
          </div>

          <!-- Filtros -->
          <div class="vb-card">
            <p class="vb-secondary" style="font-size:0.8rem;margin:0 0 8px;">Filtrar historial</p>
            <div class="vb-chips">
              <span class="vb-chip" [class.active]="filter()==='all'" (click)="filter.set('all')">Todos</span>
              <span class="vb-chip" [class.active]="filter()==='DEPOSIT'" (click)="filter.set('DEPOSIT')">Ingresos</span>
              <span class="vb-chip" [class.active]="filter()==='WITHDRAWAL'" (click)="filter.set('WITHDRAWAL')">Gastos</span>
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
              <div style="text-align:center;padding:24px 0;">
                <i class="ti ti-inbox" style="font-size:38px;color:var(--text-muted);"></i>
                <p class="vb-muted" style="font-size:0.9rem;margin-top:8px;">Nada por aqui todavia. Registra tu primer movimiento.</p>
              </div>
            }
            @for (t of filtered(); track t.id) {
              <div style="display:flex;align-items:center;gap:12px;padding:11px 0;border-bottom:1px solid var(--border-soft);">
                <div style="width:36px;height:36px;border-radius:10px;background:var(--bg-card-hover);display:flex;align-items:center;justify-content:center;">
                  <i class="ti {{ txIcon(t) }}" style="font-size:18px;" [style.color]="dir(t)==='in' ? 'var(--green)' : 'var(--accent)'"></i>
                </div>
                <div style="flex:1;min-width:0;">
                  <div style="font-weight:500;font-size:0.9rem;">{{ primaryText(t) }}</div>
                  <div class="vb-muted" style="font-size:0.74rem;">{{ secondaryText(t) }}</div>
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
              <button [class.active]="op()==='withdraw'" (click)="setOp('withdraw')">Gastar</button>
            </div>

            @if (op()==='transfer') {
              <label class="vb-label">Cuenta destino</label>
              <input class="vb-input" [(ngModel)]="toAccount" placeholder="ES..." style="margin-bottom:14px;" />
            }

            @if (op()==='withdraw') {
              <label class="vb-label">Categoria</label>
              <select class="vb-input" [(ngModel)]="category" style="margin-bottom:14px;">
                @for (c of categories; track c.key) { <option [value]="c.key">{{ c.label }}</option> }
              </select>
            }

            <label class="vb-label">Importe (&euro;)</label>
            <input class="vb-input" type="number" [(ngModel)]="amount" placeholder="0.00" min="0.01" step="0.01" style="margin-bottom:14px;" />

            <label class="vb-label">Concepto (opcional)</label>
            <input class="vb-input" [(ngModel)]="description" [placeholder]="conceptPlaceholder()" style="margin-bottom:18px;" />

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
  theme = inject(ThemeService);

  categories: Cat[] = [
    { key: 'COMIDA', label: 'Comida', icon: 'ti-shopping-cart' },
    { key: 'TRANSPORTE', label: 'Transporte', icon: 'ti-car' },
    { key: 'OCIO', label: 'Ocio', icon: 'ti-movie' },
    { key: 'HOGAR', label: 'Hogar', icon: 'ti-home' },
    { key: 'SALUD', label: 'Salud', icon: 'ti-heart' },
    { key: 'COMPRAS', label: 'Compras', icon: 'ti-shopping-bag' },
    { key: 'SERVICIOS', label: 'Servicios', icon: 'ti-bolt' },
    { key: 'OTROS', label: 'Otros', icon: 'ti-dots' },
  ];

  profile = signal<Profile | null>(null);
  transactions = signal<Transaction[]>([]);
  summary = signal<CategorySummary[]>([]);
  avatar = signal<string | null>(null);

  op = signal<Op>('withdraw');
  filter = signal<Filter>('all');

  toAccount = '';
  amount: number | null = null;
  category = 'COMIDA';
  description = '';
  error = signal<string | null>(null);
  ok = signal<string | null>(null);
  loading = signal(false);

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
    this.bank.getProfile().subscribe({ next: p => { this.profile.set(p); this.loadAvatar(); } });
    this.bank.getTransactions().subscribe({ next: page => this.transactions.set(page.content) });
    this.bank.getSummary().subscribe({ next: s => this.summary.set(s) });
  }

  setOp(o: Op): void {
    this.op.set(o);
    this.error.set(null);
    this.ok.set(null);
  }

  btnLabel(): string {
    return this.op() === 'transfer' ? 'Enviar dinero' : this.op() === 'deposit' ? 'Ingresar' : 'Registrar gasto';
  }

  conceptPlaceholder(): string {
    return this.op() === 'withdraw' ? 'Ej: Mercadona, gasolina...' : this.op() === 'deposit' ? 'Ej: Nomina' : 'Ej: Alquiler';
  }

  submit(): void {
    this.error.set(null);
    this.ok.set(null);
    if (!this.amount || this.amount <= 0) { this.error.set('Indica un importe valido'); return; }
    if (this.op() === 'transfer' && !this.toAccount) { this.error.set('Indica la cuenta destino'); return; }
    this.loading.set(true);

    const done = (msg: string) => {
      this.loading.set(false);
      this.ok.set(msg);
      this.toAccount = '';
      this.amount = null;
      this.description = '';
      this.reload();
    };
    const fail = (e: { error?: { message?: string; error?: string } }) => {
      this.loading.set(false);
      this.error.set(e?.error?.message || e?.error?.error || 'Operacion fallida');
    };

    if (this.op() === 'transfer') {
      this.bank.transfer(this.toAccount, this.amount, this.description).subscribe({ next: () => done('Transferencia realizada'), error: fail });
    } else if (this.op() === 'deposit') {
      this.bank.deposit(this.amount, 'INGRESO', this.description).subscribe({ next: () => done('Ingreso realizado'), error: fail });
    } else {
      this.bank.withdraw(this.amount, this.category, this.description).subscribe({ next: () => done('Gasto registrado'), error: fail });
    }
  }

  barPct(s: CategorySummary): number {
    const max = Math.max(...this.summary().map(x => x.total), 1);
    return Math.round((s.total / max) * 100);
  }

  iconFor(cat: string | null): string {
    const c = this.categories.find(x => x.key === cat);
    if (c) return c.icon;
    if (cat === 'INGRESO') return 'ti-cash';
    return 'ti-circle';
  }

  labelFor(cat: string | null): string {
    const c = this.categories.find(x => x.key === cat);
    if (c) return c.label;
    return cat === 'INGRESO' ? 'Ingreso' : (cat || 'Otros');
  }

  txIcon(t: Transaction): string {
    if (t.type === 'DEPOSIT') return 'ti-arrow-down-circle';
    if (t.type === 'WITHDRAWAL') return this.iconFor(t.category);
    return 'ti-arrows-exchange';
  }

  primaryText(t: Transaction): string {
    if (t.description) return t.description;
    if (t.type === 'DEPOSIT') return 'Ingreso';
    if (t.type === 'WITHDRAWAL') return this.labelFor(t.category);
    return this.dir(t) === 'in' ? 'Transferencia recibida' : 'Transferencia enviada';
  }

  secondaryText(t: Transaction): string {
    if (t.type === 'DEPOSIT') return 'Ingreso de efectivo';
    if (t.type === 'WITHDRAWAL') return this.labelFor(t.category);
    return this.dir(t) === 'in' ? (t.fromAccountNumber ?? 'Banco') : (t.toAccountNumber ?? '-');
  }

  cardNumber(): string {
    const acct = this.profile()?.accountNumber ?? '';
    return '••••  ••••  ••••  ' + (acct.slice(-4) || '0000');
  }

  dir(t: Transaction): 'in' | 'out' {
    if (t.type === 'DEPOSIT') return 'in';
    if (t.type === 'WITHDRAWAL') return 'out';
    return t.toAccountNumber === this.profile()?.accountNumber ? 'in' : 'out';
  }

  displayName(): string {
    return this.profile()?.fullName || this.profile()?.email || 'Usuario';
  }

  initials(): string {
    const name = this.profile()?.fullName || this.profile()?.email || '?';
    const parts = name.trim().split(/\s+/);
    return (parts.length > 1 ? parts[0][0] + parts[1][0] : name.slice(0, 2)).toUpperCase();
  }

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

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
