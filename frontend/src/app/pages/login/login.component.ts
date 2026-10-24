import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  template: `
    <div class="vb-shell" style="min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;">
      <div class="vb-card" style="width:100%;max-width:380px;">
        <div class="vb-brand" style="text-align:center;margin-bottom:4px;">Valencia<span>Bank</span></div>
        <p class="vb-muted" style="text-align:center;margin:0 0 24px;font-size:0.8rem;">Demo · dinero de juguete</p>

        @if (mode() === 'register') {
          <label class="vb-label">Nombre</label>
          <input class="vb-input" [(ngModel)]="fullName" placeholder="Tu nombre" style="margin-bottom:16px;" />
          <label class="vb-label">Localidad</label>
          <input class="vb-input" [(ngModel)]="locality" placeholder="Tu ciudad" style="margin-bottom:16px;" />
        }

        <label class="vb-label">Email</label>
        <input class="vb-input" type="email" [(ngModel)]="email" placeholder="tu@email.com" style="margin-bottom:16px;" />

        <label class="vb-label">Contraseña</label>
        <input class="vb-input" type="password" [(ngModel)]="password" placeholder="mínimo 8 caracteres" (keyup.enter)="submit()" style="margin-bottom:20px;" />

        <button class="vb-btn" (click)="submit()" [disabled]="loading()">
          {{ loading() ? 'Un momento...' : (mode() === 'login' ? 'Entrar' : 'Crear cuenta') }}
        </button>

        @if (error()) { <p class="vb-error">{{ error() }}</p> }
        @if (ok()) { <p class="vb-error vb-ok">{{ ok() }}</p> }

        <p style="text-align:center;margin:20px 0 0;">
          <span class="vb-muted" style="font-size:0.85rem;">
            {{ mode() === 'login' ? '¿No tienes cuenta?' : '¿Ya tienes cuenta?' }}
          </span>
          <button class="vb-link" (click)="toggle()" style="margin-left:6px;">
            {{ mode() === 'login' ? 'Regístrate' : 'Inicia sesión' }}
          </button>
        </p>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  mode = signal<'login' | 'register'>('login');
  email = '';
  password = '';
  fullName = '';
  locality = '';
  error = signal<string | null>(null);
  ok = signal<string | null>(null);
  loading = signal(false);

  toggle(): void {
    this.mode.set(this.mode() === 'login' ? 'register' : 'login');
    this.error.set(null);
    this.ok.set(null);
  }

  submit(): void {
    this.error.set(null);
    this.ok.set(null);
    if (!this.email || !this.password) {
      this.error.set('Rellena email y contraseña');
      return;
    }
    this.loading.set(true);

    if (this.mode() === 'login') {
      this.auth.login(this.email, this.password).subscribe({
        next: () => {
          this.loading.set(false);
          this.router.navigate(['/dashboard']);
        },
        error: e => {
          this.loading.set(false);
          this.error.set(this.msg(e));
        },
      });
    } else {
      this.auth.register(this.email, this.password, this.fullName, this.locality).subscribe({
        next: () => {
          this.loading.set(false);
          this.ok.set('Cuenta creada con 1000 € de bienvenida. Ya puedes entrar.');
          this.mode.set('login');
        },
        error: e => {
          this.loading.set(false);
          this.error.set(this.msg(e));
        },
      });
    }
  }

  private msg(e: { error?: { message?: string; error?: string } }): string {
    return e?.error?.message || e?.error?.error || 'Algo salió mal';
  }
}
