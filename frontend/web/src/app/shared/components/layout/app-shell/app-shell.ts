import { Component, HostListener, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { HeaderComponent } from '../header/header';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="erp-shell">
      @if (mobileSidebarOpen()) {
        <button class="sidebar-backdrop" type="button" aria-label="Close navigation" (click)="closeMobileSidebar()"></button>
      }
      <aside class="sidebar-panel" [class.collapsed]="sidebarCollapsed() && !mobileSidebarOpen()" [class.mobile-open]="mobileSidebarOpen()">
        <app-sidebar [collapsed]="sidebarCollapsed() && !mobileSidebarOpen()" (navigate)="closeMobileSidebar()"></app-sidebar>
      </aside>
      <div class="workspace-panel">
        <app-header [sidebarExpanded]="sidebarExpanded" (navigationToggle)="toggleNavigation()"></app-header>
        <main class="content-panel">
          <router-outlet></router-outlet>
        </main>
        <footer class="app-footer">
          <p>© 2026 Kavith Palansuriya. All rights reserved.</p>
        </footer>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        min-height: 100vh;
        background: var(--app-bg);
      }
      .erp-shell {
        display: flex;
        min-height: 100vh;
        background: var(--app-bg);
      }
      .sidebar-panel {
        position: relative;
        z-index: 1;
        width: 260px;
        flex: 0 0 260px;
        min-width: 0;
        background: var(--sidebar-bg);
        border-right: 1px solid rgb(255 255 255 / 10%);
        transition: width var(--transition-fast), flex-basis var(--transition-fast);
      }
      .sidebar-panel.collapsed { width: 76px; flex-basis: 76px; }
      .sidebar-backdrop { display: none; }
      .workspace-panel {
        flex: 1;
        min-width: 0;
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        background: var(--app-bg);
      }
      .content-panel {
        flex: 1 0 auto;
        min-width: 0;
        padding: 1.2rem 1.4rem 2rem;
      }
      .app-footer {
        flex: 0 0 auto;
        padding: 0.85rem 1.4rem;
        border-top: 1px solid var(--border-color);
        background: var(--header-bg);
        color: var(--secondary-text);
        font-size: 0.8rem;
        text-align: center;
      }
      .app-footer p {
        margin: 0;
      }
      @media (max-width: 900px) {
        .erp-shell { display: block; min-height: 100dvh; }
        .sidebar-backdrop {
          display: block;
          position: fixed;
          inset: 0;
          z-index: 40;
          width: 100%;
          height: 100%;
          border: 0;
          background: rgb(23 37 84 / 48%);
        }
        .sidebar-panel {
          display: block;
          position: fixed;
          inset: 0 auto 0 0;
          z-index: 41;
          width: min(86vw, 292px);
          height: 100dvh;
          transform: translateX(-105%);
          visibility: hidden;
          box-shadow: none;
          transition: transform 180ms ease, visibility 180ms ease, box-shadow 180ms ease;
        }
        .sidebar-panel.mobile-open {
          width: min(86vw, 292px);
          transform: translateX(0);
          visibility: visible;
          box-shadow: 10px 0 30px rgb(23 37 84 / 22%);
        }
        .sidebar-panel.collapsed { width: min(86vw, 292px); }
        .workspace-panel { min-height: 100dvh; }
        .content-panel { padding: 1rem 1rem 1.5rem; }
        .app-footer { padding: 0.8rem 1rem; }
      }
      @media (prefers-reduced-motion: reduce) { .sidebar-panel { transition: none; } }
    `,
  ],
})
export class AppShellComponent {
  private readonly router = inject(Router);
  readonly sidebarCollapsed = signal(false);
  readonly mobileSidebarOpen = signal(false);

  constructor() {
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      takeUntilDestroyed(),
    ).subscribe(() => this.mobileSidebarOpen.set(false));
  }

  get sidebarExpanded(): boolean {
    return this.isMobileViewport() ? this.mobileSidebarOpen() : !this.sidebarCollapsed();
  }

  toggleNavigation(): void {
    if (this.isMobileViewport()) {
      this.mobileSidebarOpen.update((open) => !open);
    } else {
      this.sidebarCollapsed.update((collapsed) => !collapsed);
    }
  }

  closeMobileSidebar(): void {
    if (this.isMobileViewport()) this.mobileSidebarOpen.set(false);
  }

  @HostListener('document:keydown.escape')
  closeSidebarOnEscape(): void {
    this.mobileSidebarOpen.set(false);
  }

  private isMobileViewport(): boolean {
    return window.matchMedia('(max-width: 900px)').matches;
  }
}
