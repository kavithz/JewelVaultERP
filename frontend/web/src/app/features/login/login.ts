import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthMode, AuthService, PREVIEW_ACCESS_PASSWORD } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

const PREVIEW_CREDENTIALS_TEXT = [
  'JewelVault ERP - Preview Credentials',
  '',
  'Username:',
  'Any non-empty username',
  '',
  'Password:',
  PREVIEW_ACCESS_PASSWORD,
  '',
  'Environment:',
  'Development / Preview only',
].join('\n');

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="login-shell">
      <div class="login-layout">
        <section class="brand-panel" aria-label="JewelVaultERP">
          <div class="brand-lockup">
            <img src="/logo-mark.svg" alt="" aria-hidden="true" />
            <div class="brand-name">JewelVault<span>ERP</span></div>
          </div>
          <div class="brand-message">
            <p class="eyebrow">JEWELLERY OPERATIONS</p>
            <h1>Every detail,<br />in its place.</h1>
            <p class="brand-copy">A clear workspace for the people, products, and movement behind your business.</p>
          </div>
          <div class="gem-feature" aria-hidden="true">
            <span class="facet facet-one"></span>
            <span class="facet facet-two"></span>
            <img src="/logo-mark.svg" alt="" />
          </div>
          <p class="brand-caption">A considered view of your operation.</p>
        </section>

        <section class="login-panel" aria-labelledby="login-heading">
          <div class="mobile-lockup">
            <img src="/logo.svg" alt="JewelVaultERP" />
          </div>
          <p class="eyebrow">WORKSPACE ACCESS</p>
          <h2 id="login-heading">Sign in</h2>
          <p class="intro">{{ authMode() === 'preview' ? 'Use preview access to explore the application.' : 'Sign in with your organization account.' }}</p>

          <fieldset class="auth-mode">
            <legend>Authentication mode</legend>
            <label><input type="radio" name="authentication-mode" [checked]="authMode() === 'preview'" (change)="setAuthMode('preview')" />Preview</label>
            <label><input type="radio" name="authentication-mode" [checked]="authMode() === 'real'" (change)="setAuthMode('real')" />Sign in</label>
          </fieldset>

          @if (authMode() === 'preview') {
            <div class="preview-note" role="note">
              <span class="preview-dot" aria-hidden="true"></span>
              NOTE: Preview Mode Data is not real and is not related to our system.
            </div>
          }

          @if (errorMessage()) {
            <div class="alert" role="alert">{{ errorMessage() }}</div>
          }

          <form [formGroup]="form" (ngSubmit)="submit()">
            <label for="login-username">Username</label>
            <input id="login-username" formControlName="username" type="text" autocomplete="username" placeholder="Enter username" required />

            <label for="login-password">Password</label>
            <input id="login-password" formControlName="password" type="password" autocomplete="current-password" placeholder="Enter password" required />

            <button type="submit" [disabled]="form.invalid || submitting()">{{ submitting() ? 'Signing in...' : 'Sign in' }}</button>
            @if (authMode() === 'preview') {
              <a class="credentials-download" href="#" (click)="$event.preventDefault(); downloadPreviewCredentials()">
                <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true"><use href="/icons.svg#text-file"></use></svg>
                <span>Download preview credentials</span>
              </a>
            }
          </form>
        </section>
      </div>
    </section>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; }
      .login-shell {
        display: grid;
        place-items: center;
        min-height: 100dvh;
        padding: clamp(1rem, 4vw, 3rem);
        background: var(--app-bg);
      }
      .login-layout {
        display: grid;
        grid-template-columns: minmax(0, 1.05fr) minmax(20rem, 0.95fr);
        width: min(100%, 64rem);
        min-height: 39rem;
        overflow: hidden;
        border: 1px solid var(--border-color);
        border-radius: 0.9rem;
        background: var(--surface);
        box-shadow: var(--shadow-md);
      }
      .brand-panel {
        position: relative;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        min-width: 0;
        overflow: hidden;
        padding: 2.25rem;
        background: var(--primary-dark);
        color: #f8fafc;
      }
      .brand-lockup {
        display: flex;
        align-items: center;
        gap: 0.65rem;
      }
      .brand-lockup img { width: 2.55rem; height: 2.55rem; }
      .brand-name { color: #fffefa; font-size: 1.2rem; font-weight: 600; letter-spacing: 0; }
      .brand-name span { margin-left: 0.35rem; color: var(--gold); font-size: 0.68rem; font-weight: 600; letter-spacing: 0.12em; }
      .brand-message { position: relative; z-index: 1; max-width: 27rem; margin-block: auto; padding-block: 3rem; }
      .eyebrow { margin: 0 0 0.55rem; color: var(--gold); font-size: 0.68rem; font-weight: 600; letter-spacing: 0.09em; text-transform: uppercase; }
      .brand-message h1 { margin: 0; color: #fffefa; font-size: 2.25rem; line-height: 1.13; font-weight: 600; }
      .brand-copy { max-width: 25rem; margin: 1rem 0 0; color: #d6deed; font-size: 0.95rem; line-height: 1.65; }
      .gem-feature { position: absolute; right: 2rem; bottom: 4rem; width: 9rem; height: 9rem; display: grid; place-items: center; border: 1px solid rgb(201 162 39 / 28%); border-radius: 1rem; transform: rotate(45deg); }
      .gem-feature img { width: 7.3rem; height: 7.3rem; transform: rotate(-45deg); opacity: 0.96; }
      .facet { position: absolute; inset: 1.25rem; border: 1px solid rgb(248 250 252 / 14%); border-radius: 0.6rem; }
      .facet-two { inset: 2.1rem; border-color: rgb(4 120 87 / 50%); }
      .brand-caption { position: relative; z-index: 1; margin: 0; color: #b8c5df; font-size: 0.75rem; }
      .login-panel { align-self: center; width: min(100%, 27rem); margin-inline: auto; padding: 2.5rem; }
      .login-panel > .eyebrow { color: var(--gold); }
      .login-panel h2 { margin: 0; color: var(--primary-text); font-size: 1.8rem; line-height: 1.2; font-weight: 600; }
      .intro { margin: 0.45rem 0 1.25rem; color: var(--secondary-text); font-size: 0.9rem; }
      .preview-note { display: flex; align-items: flex-start; gap: 0.55rem; margin-bottom: 1.3rem; padding: 0.7rem 0.75rem; border: 1px solid #eadcae; border-radius: 0.5rem; background: #fbf7e8; color: #644f10; font-size: 0.78rem; line-height: 1.45; }
      .preview-dot { width: 0.5rem; height: 0.5rem; flex: 0 0 auto; margin-top: 0.25rem; border-radius: 50%; background: var(--gold); }
      .mobile-lockup { display: none; }
      form { display: grid; gap: 0.55rem; }
      label { margin-top: 0.55rem; color: var(--primary-text); font-size: 0.82rem; font-weight: 500; }
      input { width: 100%; min-height: 2.9rem; padding: 0.65rem 0.75rem; border: 1px solid var(--border-color); border-radius: 0.5rem; background: var(--surface-soft); color: var(--primary-text); font: inherit; font-size: 0.9rem; }
      input::placeholder { color: var(--text-muted); }
      input:focus-visible { border-color: var(--primary); outline: 3px solid var(--primary-light); outline-offset: 1px; }
      button[type='submit'] { min-height: 2.85rem; margin-top: 0.85rem; border: 1px solid var(--primary); border-radius: 0.5rem; background: var(--primary); color: #fff; font: inherit; font-size: 0.9rem; font-weight: 600; cursor: pointer; transition: var(--transition-fast); }
      button[type='submit']:hover:not(:disabled) { border-color: var(--primary-dark); background: var(--primary-dark); }
      button[type='submit']:disabled { opacity: 0.6; cursor: not-allowed; }
      .alert { margin-bottom: 1rem; padding: 0.7rem 0.8rem; border: 1px solid rgb(185 28 28 / 16%); border-radius: 0.5rem; background: var(--ruby-light); color: var(--ruby); font-size: 0.85rem; }
      @media (max-width: 760px) {
        .login-shell { place-items: start center; padding: 1rem; }
        .login-layout { grid-template-columns: minmax(0, 1fr); width: min(100%, 31rem); min-height: 0; }
        .brand-panel { display: none; }
        .login-panel { width: 100%; padding: 1.5rem; }
        .mobile-lockup { display: block; margin-bottom: 1.5rem; }
        .mobile-lockup img { display: block; width: min(100%, 14rem); height: auto; }
      }
      @media (max-width: 390px) { .login-panel { padding: 1.1rem; } }
    `,
  ],
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  readonly authMode = signal<AuthMode>(environment.authMode);
  readonly submitting = signal(false);
  readonly errorMessage = signal('');

  setAuthMode(mode: AuthMode): void {
    this.authMode.set(mode);
    this.errorMessage.set('');
  }

  downloadPreviewCredentials(): void {
    const file = new Blob([PREVIEW_CREDENTIALS_TEXT], { type: 'text/plain;charset=utf-8' });
    const downloadUrl = URL.createObjectURL(file);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = 'jewelvault-preview-credentials.txt';
    document.body.append(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(downloadUrl), 0);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');

    const username = this.form.value.username ?? '';
    const password = this.form.value.password ?? '';
    const loginRequest = this.authMode() === 'preview'
      ? this.authService.loginPreview(username, password)
      : this.authService.loginReal(username, password);

    loginRequest.subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: (error: Error) => {
        this.errorMessage.set(error.message || 'Unable to sign in right now. Please try again.');
        this.submitting.set(false);
      },
      complete: () => {
        this.submitting.set(false);
      },
    });
  }
}
