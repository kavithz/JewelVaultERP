import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="topbar">
      <div class="topbar-left">
        <button
          class="icon-button nav-toggle"
          [attr.aria-expanded]="sidebarExpanded"
          aria-controls="primary-navigation"
          [attr.aria-label]="sidebarExpanded ? 'Collapse navigation' : 'Open navigation'"
          type="button"
          (click)="navigationToggle.emit()"
        >
          <svg aria-hidden="true"><use href="/icons.svg#menu"></use></svg>
        </button>
        <img class="header-mark" src="/logo-mark.svg" alt="" aria-hidden="true" />
        <div class="greeting">
          <div class="eyebrow">OPERATIONS</div>
          <h2>Hello, {{ displayName }}</h2>
          <p>Welcome back</p>
        </div>
      </div>

      <div class="topbar-actions">
        <button
          class="icon-button notification-mark"
          type="button"
          aria-label="Notifications"
          [attr.aria-expanded]="notificationsOpen"
          [attr.aria-controls]="notificationsOpen ? 'notification-panel' : null"
          (click)="toggleNotifications()"
        >
          <svg aria-hidden="true"><use href="/icons.svg#bell"></use></svg>
        </button>
        @if (notificationsOpen) {
          <div id="notification-panel" class="notification-panel" role="status" aria-live="polite">
            Notifications are not available in this preview.
          </div>
        }
        <button class="logout-button" type="button" (click)="logout()">Logout</button>
      </div>
    </header>
  `,
  styles: [
    `
      .topbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        min-height: 76px;
        padding: 0.75rem 1.4rem;
        background: var(--header-bg);
        border-bottom: 1px solid var(--border-color);
      }
      .topbar-left {
        display: flex;
        align-items: center;
        gap: 0.7rem;
        min-width: 0;
      }
      .header-mark { width: 2.35rem; height: 2.35rem; flex: 0 0 auto; }
      .greeting { min-width: 0; }
      .eyebrow {
        font-size: 0.66rem;
        text-transform: uppercase;
        letter-spacing: 0.07em;
        color: var(--secondary-text);
        font-weight: 500;
      }
      h2 {
        overflow: hidden;
        margin: 0.05rem 0 0;
        font-size: 1rem;
        line-height: 1.25;
        font-weight: 600;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: var(--primary-text);
      }
      .greeting p { margin: 0.05rem 0 0; color: var(--secondary-text); font-size: 0.72rem; line-height: 1.2; }
      .topbar-actions {
        position: relative;
        display: flex;
        align-items: center;
        gap: 0.65rem;
        flex: 0 0 auto;
      }
      .icon-button, .logout-button, .notification-mark {
        display: inline-grid;
        min-width: 2.6rem;
        min-height: 2.6rem;
        place-items: center;
        border: 1px solid var(--border-color);
        border-radius: 0.55rem;
        background: var(--surface);
        color: var(--primary);
      }
      .icon-button { cursor: pointer; }
      .icon-button:hover { background: var(--primary-light); }
      .notification-mark { font: inherit; }
      .icon-button svg { width: 1.2rem; height: 1.2rem; fill: none; stroke: currentColor; stroke-width: 1.7; stroke-linecap: round; stroke-linejoin: round; }
      .notification-panel {
        position: absolute;
        top: calc(100% + 0.55rem);
        right: 3.2rem;
        z-index: 5;
        width: min(17rem, calc(100vw - 2rem));
        padding: 0.8rem;
        border: 1px solid var(--border-color);
        border-radius: 0.5rem;
        background: var(--surface);
        box-shadow: var(--shadow-md);
        color: var(--secondary-text);
        font-size: 0.82rem;
        line-height: 1.45;
      }
      .logout-button {
        padding: 0.45rem 0.75rem;
        cursor: pointer;
        font: inherit;
        font-size: 0.84rem;
        font-weight: 500;
      }
      .logout-button:hover { background: var(--ruby-light); border-color: rgb(185 28 28 / 22%); color: var(--ruby); }
      @media (max-width: 900px) {
        .topbar { padding-inline: 1rem; }
      }
      @media (max-width: 480px) {
        .topbar { gap: 0.4rem; min-height: 68px; padding-inline: 0.65rem; }
        .topbar-left { gap: 0.4rem; }
        .header-mark { width: 1.9rem; height: 1.9rem; }
        .greeting h2 { max-width: 8.5rem; font-size: 0.86rem; }
        .greeting p { display: none; }
        .topbar-actions { gap: 0.35rem; }
        .icon-button, .logout-button { min-width: 2.4rem; min-height: 2.4rem; }
        .logout-button { padding-inline: 0.5rem; }
      }
    `,
  ],
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  @Input() sidebarExpanded = true;
  @Output() navigationToggle = new EventEmitter<void>();
  notificationsOpen = false;

  get currentUser() {
    return this.authService.currentUser();
  }

  get displayName(): string {
    return this.currentUser.username?.trim() || 'ERP User';
  }

  toggleNotifications(): void {
    this.notificationsOpen = !this.notificationsOpen;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
