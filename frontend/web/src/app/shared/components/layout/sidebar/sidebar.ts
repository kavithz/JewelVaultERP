import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <div class="sidebar-inner" [class.is-collapsed]="collapsed">
      <div class="brand-block" role="img" aria-label="JewelVault ERP">
        <img class="brand-logo" src="/logo-mark.svg" alt="" aria-hidden="true" />
        <div class="brand-copy" aria-hidden="true">
          <span class="brand-name">JewelVault</span>
          <span class="brand-subtitle">ERP</span>
        </div>
      </div>

      <nav id="primary-navigation" class="nav-section" aria-label="Main navigation" (click)="handleNavigationClick($event)">
        @for (group of navigationGroups; track group.label) {
          <section class="nav-group" [attr.aria-label]="group.label">
            <h2 class="nav-label">{{ group.label }}</h2>
            @for (item of group.items; track item.path) {
              <a [routerLink]="item.path" routerLinkActive="active" [attr.aria-label]="item.label" [attr.title]="collapsed ? item.label : null">
                <svg class="nav-icon" aria-hidden="true"><use [attr.href]="'/icons.svg#' + item.icon"></use></svg>
                <span class="nav-text">{{ item.label }}</span>
              </a>
            }
          </section>
        }
      </nav>
    </div>
  `,
  styles: [
    `
      :host { display: block; height: 100%; }
      .sidebar-inner {
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 1rem 0.75rem;
        color: var(--sidebar-text);
      }
      .brand-block {
        display: flex;
        align-items: center;
        gap: 0.6rem;
        min-height: 3.5rem;
        padding: 0.25rem 0.45rem 0.9rem;
        border-bottom: 1px solid rgb(255 255 255 / 14%);
        margin-bottom: 0.5rem;
      }
      .brand-logo { display: block; width: 2.5rem; height: 2.5rem; flex: 0 0 auto; }
      .brand-copy { display: flex; min-width: 0; flex-direction: column; gap: 0.05rem; }
      .brand-name { color: #fffefa; font-size: 0.96rem; line-height: 1.2; font-weight: 600; white-space: nowrap; }
      .brand-subtitle { color: var(--gold); font-size: 0.62rem; line-height: 1.2; font-weight: 500; letter-spacing: 0.18em; }
      .nav-section {
        display: grid;
        align-content: start;
        gap: 0.45rem;
        overflow-y: auto;
        min-height: 0;
      }
      .nav-group { display: grid; gap: 0.16rem; }
      .nav-label {
        margin: 0.7rem 0.45rem 0.25rem;
        color: var(--sidebar-muted);
        font-size: 0.66rem;
        text-transform: uppercase;
        letter-spacing: 0.07em;
        font-weight: 500;
      }
      a {
        display: flex;
        align-items: center;
        gap: 0.7rem;
        min-height: 2.45rem;
        padding: 0.48rem 0.65rem;
        border-left: 3px solid transparent;
        border-radius: 0.45rem;
        color: var(--sidebar-text);
        text-decoration: none;
        transition: var(--transition-fast);
        font-size: 0.84rem;
        font-weight: 400;
      }
      .nav-icon { width: 1.1rem; height: 1.1rem; flex: 0 0 auto; fill: none; stroke: currentColor; stroke-width: 1.65; stroke-linecap: round; stroke-linejoin: round; opacity: 0.9; }
      a:hover { background: rgb(255 255 255 / 8%); }
      a.active {
        border-left-color: var(--gold);
        background: var(--sidebar-active);
        color: var(--gold-light);
        font-weight: 500;
      }
      .is-collapsed .brand-block { justify-content: center; padding-inline: 0; }
      .is-collapsed .brand-copy, .is-collapsed .nav-label, .is-collapsed .nav-text { display: none; }
      .is-collapsed a { justify-content: center; padding-inline: 0.4rem; }
      .is-collapsed .nav-icon { width: 1.2rem; height: 1.2rem; }
      @media (max-width: 900px) {
        .sidebar-inner { padding: 1rem 0.75rem; }
        .is-collapsed .brand-block { justify-content: flex-start; padding-inline: 0.45rem; }
        .is-collapsed .brand-copy { display: flex; }
        .is-collapsed .nav-text { display: initial; }
        .is-collapsed .nav-label { display: block; }
        .is-collapsed a { justify-content: flex-start; padding-inline: 0.65rem; }
      }
    `,
  ],
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Output() navigate = new EventEmitter<void>();

  readonly navigationGroups = [
    { label: 'Overview', items: [{ label: 'Dashboard', path: '/dashboard', icon: 'dashboard' }] },
    { label: 'Master Data', items: [
      { label: 'Companies', path: '/companies', icon: 'building' },
      { label: 'Users', path: '/users', icon: 'users' },
      { label: 'Branches', path: '/branches', icon: 'branch' },
      { label: 'Products', path: '/products', icon: 'boxes' },
      { label: 'Purities', path: '/purities', icon: 'gem' },
      { label: 'Jewellery Types', path: '/jewellery-types', icon: 'gem' },
      { label: 'Gemstones', path: '/gemstones', icon: 'gem' },
      { label: 'Warehouses', path: '/warehouses', icon: 'warehouse' },
      { label: 'Suppliers', path: '/suppliers', icon: 'truck' },
      { label: 'Customers', path: '/customers', icon: 'users' },
    ] },
    { label: 'Inventory', items: [
      { label: 'Inventory', path: '/inventory', icon: 'boxes' },
      { label: 'Stock Movements', path: '/stock-movements', icon: 'movement' },
      { label: 'Stock Adjustments', path: '/stock-adjustments', icon: 'receipt' },
      { label: 'Stock Transfers', path: '/stock-transfers', icon: 'movement' },
    ] },
    { label: 'Sales', items: [{ label: 'Sales', path: '/sales', icon: 'receipt' }] },
    { label: 'Purchasing', items: [{ label: 'Purchases', path: '/purchases', icon: 'bag' }] },
    { label: 'People', items: [{ label: 'Employees', path: '/employees', icon: 'person' }] },
  ];

  handleNavigationClick(event: MouseEvent): void {
    if (event.target instanceof Element && event.target.closest('#primary-navigation a[href]')) {
      this.navigate.emit();
    }
  }

}
