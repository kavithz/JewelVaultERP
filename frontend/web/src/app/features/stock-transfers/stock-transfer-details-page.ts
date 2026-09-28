import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { take } from 'rxjs';
import { StockTransferRecord } from '../../core/models/erp.models';
import { StockTransferService } from '../../core/services/stock-transfer.service';
import { DataTableColumn, DataTableComponent } from '../../shared/components/data-table/data-table';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state';
import { LoadingStateComponent } from '../../shared/components/loading-state/loading-state';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-stock-transfer-details-page',
  standalone: true,
  imports: [RouterLink, PageHeaderComponent, DataTableComponent, ErrorStateComponent, LoadingStateComponent],
  template: `
    <app-page-header [title]="transfer() ? 'Transfer ' + transfer()!.transferNumber : 'Transfer details'" subtitle="Transfer record and item quantities." />
    <a class="back-link" routerLink="/stock-transfers">Back to Stock Transfers</a>

    @if (loading()) {
      <app-loading-state />
    } @else if (error()) {
      <app-error-state [message]="error()!" />
    } @else if (transfer(); as record) {
      <section class="details-panel" aria-labelledby="transfer-details-heading">
        <div class="heading-row">
          <h2 id="transfer-details-heading">Transfer details</h2>
          <span class="status" [class.completed]="record.status === 'COMPLETED'" [class.cancelled]="record.status === 'CANCELLED'">{{ record.status }}</span>
        </div>
        <dl class="details-grid">
          <div><dt>Transfer number</dt><dd>{{ record.transferNumber }}</dd></div>
          <div><dt>Transfer date</dt><dd>{{ record.transferDate }}</dd></div>
          <div><dt>Company ID</dt><dd>{{ record.companyId }}</dd></div>
          <div><dt>Source warehouse ID</dt><dd>{{ record.sourceWarehouseId }}</dd></div>
          <div><dt>Destination warehouse ID</dt><dd>{{ record.destinationWarehouseId }}</dd></div>
          <div><dt>Created</dt><dd>{{ record.createdAt }}</dd></div>
          <div><dt>Last updated</dt><dd>{{ record.updatedAt }}</dd></div>
          <div class="wide"><dt>Notes</dt><dd>{{ record.notes ?? '—' }}</dd></div>
        </dl>
      </section>
      <section class="item-section" aria-labelledby="transfer-items-heading">
        <h2 id="transfer-items-heading">Transfer items</h2>
        <app-data-table
          [columns]="itemColumns"
          [rows]="itemRows()"
          [loading]="false"
          emptyTitle="No transfer items found."
          emptyMessage="This transfer response contains no items."
        />
      </section>
    }
  `,
  styles: [`
    :host { display: block; min-width: 0; }
    .back-link { display: inline-block; margin: 0 0 1rem; color: var(--accent-strong); }
    .details-panel, .item-section { min-width: 0; padding: 1rem; border: 1px solid var(--border-color); border-radius: 0.45rem; background: #fff; }
    .details-panel { margin-bottom: 1rem; }
    .heading-row { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 0.8rem; }
    h2 { margin: 0 0 0.8rem; color: var(--primary-text); font-size: 1.05rem; }
    .heading-row h2 { margin: 0; }
    .details-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.8rem 1.4rem; margin: 0; }
    .details-grid > div { min-width: 0; }
    .details-grid .wide { grid-column: 1 / -1; }
    dt { color: var(--secondary-text); font-size: 0.8rem; }
    dd { overflow-wrap: anywhere; margin: 0.2rem 0 0; color: var(--primary-text); }
    .status { padding: 0.25rem 0.5rem; border-radius: 0.4rem; background: var(--surface-soft); border: 1px solid var(--border-color); color: var(--secondary-text); font-size: 0.75rem; font-weight: 500; }
    .status.completed { background: rgba(32, 170, 124, 0.12); color: var(--success); }
    .status.cancelled { background: rgba(214, 81, 81, 0.1); color: var(--danger); }
    @media (max-width: 600px) { .details-grid { grid-template-columns: minmax(0, 1fr); } .details-grid .wide { grid-column: auto; } }
  `],
})
export class StockTransferDetailsPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly transferService = inject(StockTransferService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly transfer = signal<StockTransferRecord | null>(null);
  readonly itemRows = signal<Record<string, unknown>[]>([]);

  readonly itemColumns: DataTableColumn[] = [
    { key: 'productId', label: 'Product ID' },
    { key: 'quantity', label: 'Quantity' },
  ];

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Transfer ID is missing from the route.');
      this.loading.set(false);
      return;
    }

    this.transferService.get(id).pipe(take(1)).subscribe({
      next: (transfer) => {
        this.transfer.set(transfer);
        this.itemRows.set(transfer.items.map((item) => ({ productId: item.productId, quantity: item.quantity })));
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(error instanceof Error ? error.message : 'Unable to load transfer details.');
        this.loading.set(false);
      },
    });
  }
}