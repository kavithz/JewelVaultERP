import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { KpiCardComponent } from '../../shared/components/kpi-card/kpi-card';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { CardComponent } from '../../shared/components/card/card';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state';
import { LoadingStateComponent } from '../../shared/components/loading-state/loading-state';
import { CompanyService } from '../../core/services/company.service';
import { DashboardService, DashboardSummary } from '../../core/services/dashboard.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    KpiCardComponent,
    PageHeaderComponent,
    CardComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    LoadingStateComponent,
  ],
  template: `
    <app-page-header title="Dashboard" subtitle="Operational overview for current company activity." />

    @if (loading()) {
      <app-loading-state></app-loading-state>
    } @else if (error()) {
      <app-error-state [message]="error() ?? 'Unable to load this data. Please try again.'"></app-error-state>
    } @else if (summary(); as summary) {
      <section class="kpi-grid">
        <app-kpi-card label="Total sales" [value]="formatCurrency(summary.totalSales)" icon="receipt" tone="gold"></app-kpi-card>
        <app-kpi-card label="Purchases" [value]="formatCurrency(summary.totalPurchases)" icon="bag" tone="emerald"></app-kpi-card>
        <app-kpi-card label="Expenses" [value]="formatCurrency(summary.totalExpenses)" icon="receipt" tone="amethyst"></app-kpi-card>
        <app-kpi-card label="Inventory value" [value]="formatCurrency(summary.inventoryValue)" icon="boxes" tone="blue"></app-kpi-card>
        <app-kpi-card label="Receivables" [value]="formatCurrency(summary.outstandingReceivables)" icon="building" tone="amethyst"></app-kpi-card>
        <app-kpi-card label="Payables" [value]="formatCurrency(summary.outstandingPayables)" icon="bag" tone="emerald"></app-kpi-card>
      </section>

      <section class="content-grid">
        <app-card title="Sales summary">
          <dl class="stats-list">
            <div><dt>Today</dt><dd>{{ formatCurrency(summary.salesToday) }}</dd></div>
            <div><dt>This month</dt><dd>{{ formatCurrency(summary.salesThisMonth) }}</dd></div>
            <div><dt>Customers</dt><dd>{{ summary.totalCustomers }}</dd></div>
          </dl>
        </app-card>

        <app-card title="Purchases summary">
          <dl class="stats-list">
            <div><dt>Today</dt><dd>{{ formatCurrency(summary.totalPurchases) }}</dd></div>
            <div><dt>Suppliers</dt><dd>{{ summary.totalSuppliers }}</dd></div>
            <div><dt>Products</dt><dd>{{ summary.totalProducts }}</dd></div>
          </dl>
        </app-card>

        <app-card title="Inventory overview">
          <dl class="stats-list">
            <div><dt>Stock value</dt><dd>{{ formatCurrency(summary.inventoryValue) }}</dd></div>
            <div><dt>Active users</dt><dd>{{ summary.activeUsers }}</dd></div>
            <div><dt>Employees</dt><dd>{{ summary.activeEmployees }}</dd></div>
          </dl>
        </app-card>

        <app-card title="Operations snapshot">
          <ul class="activity-list">
            <li>{{ summary.activeEmployees }} active employees.</li>
            <li>{{ summary.activeUsers }} active users.</li>
            <li>{{ summary.unreadNotifications }} unread notifications.</li>
          </ul>
        </app-card>
      </section>
    } @else {
      <app-empty-state title="Dashboard unavailable" message="No company summary is available yet." />
    }
  `,
  styles: [
    `
      .kpi-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(min(100%, 12.5rem), 1fr));
        gap: 0.85rem;
        margin-bottom: 1.2rem;
      }
      .content-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(min(100%, 16rem), 1fr));
        gap: 0.85rem;
      }
      .stats-list {
        display: grid;
        gap: 0.8rem;
        margin: 0;
      }
      .stats-list div {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        border-bottom: 1px solid var(--border-color);
        padding-bottom: 0.45rem;
      }
      .stats-list dt { color: var(--secondary-text); }
      .stats-list dd { margin: 0; font-size: 0.9rem; font-weight: 600; color: var(--primary-text); }
      .activity-list {
        margin: 0;
        padding-left: 1rem;
        display: grid;
        gap: 0.55rem;
        color: var(--secondary-text);
        font-size: 0.88rem;
      }
    `,
  ],
})
export class DashboardPageComponent {
  private readonly companyService = inject(CompanyService);
  private readonly dashboardService = inject(DashboardService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly summary = signal<DashboardSummary | null>(null);

  constructor() {
    this.loadDashboard();
  }

  formatCurrency(value: number | null | undefined): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);
  }

  private loadDashboard(): void {
    this.companyService.list().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (companies) => {
        const companyId = companies[0]?.id ?? environment.defaultCompanyId;
        this.dashboardService.getSummary(companyId).subscribe({
          next: (summary) => this.summary.set(summary),
          error: (err: Error) => this.error.set(err.message || 'Unable to load the dashboard.'),
        });
      },
      error: (err: Error) => this.error.set(err.message || 'Unable to load the company context.'),
    });
  }
}
