import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, of, take } from 'rxjs';
import { Company, Product, PurchaseRecord, Supplier, Warehouse } from '../../core/models/erp.models';
import { CompanyService } from '../../core/services/company.service';
import { ProductService } from '../../core/services/product.service';
import { PurchaseService } from '../../core/services/purchase.service';
import { SupplierService } from '../../core/services/supplier.service';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableComponent } from '../../shared/components/data-table/data-table';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state';
import { LoadingStateComponent } from '../../shared/components/loading-state/loading-state';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-purchase-details-page',
  standalone: true,
  imports: [CommonModule, RouterLink, PageHeaderComponent, DataTableComponent, ErrorStateComponent, LoadingStateComponent],
  template: `
    <app-page-header [title]="purchase() ? 'Purchase ' + purchase()!.purchaseNumber : 'Purchase details'" subtitle="Purchase record, totals, and line items." />
    <a class="back-link" routerLink="/purchases">Back to Purchases</a>

    @if (loading()) {
      <app-loading-state />
    } @else if (error()) {
      <app-error-state [message]="error()!" />
    } @else if (purchase(); as record) {
      @if (referenceWarning()) { <p class="reference-warning" role="status">{{ referenceWarning() }}</p> }
      <section class="purchase-summary" aria-labelledby="purchase-summary-heading">
        <div class="summary-heading">
          <h2 id="purchase-summary-heading">Purchase summary</h2>
          <span class="status" [class.received]="record.status === 'RECEIVED'" [class.cancelled]="record.status === 'CANCELLED'">{{ record.status }}</span>
        </div>
        <dl class="summary-grid">
          <div><dt>Purchase number</dt><dd>{{ record.purchaseNumber }}</dd></div>
          <div><dt>Purchase date</dt><dd>{{ record.purchaseDate }}</dd></div>
          <div><dt>Company</dt><dd>{{ companyName() }} <span class="identifier">{{ record.companyId }}</span></dd></div>
          <div><dt>Supplier</dt><dd>{{ supplierName() }} <span class="identifier">{{ record.supplierId }}</span></dd></div>
          <div><dt>Warehouse</dt><dd>{{ warehouseName() }} <span class="identifier">{{ record.warehouseId }}</span></dd></div>
          <div><dt>Created</dt><dd>{{ record.createdAt }}</dd></div>
          <div><dt>Subtotal</dt><dd>{{ record.subtotal }}</dd></div>
          <div><dt>Tax</dt><dd>{{ record.taxAmount }}</dd></div>
          <div><dt>Discount</dt><dd>{{ record.discountAmount }}</dd></div>
          <div><dt>Total</dt><dd class="total">{{ record.totalAmount }}</dd></div>
        </dl>
      </section>
      <section class="item-section" aria-labelledby="purchase-items-heading">
        <h2 id="purchase-items-heading">Purchase items</h2>
        <app-data-table
          [columns]="itemColumns"
          [rows]="itemRows()"
          [loading]="false"
          emptyTitle="No purchase items found."
          emptyMessage="This purchase response contains no items."
        />
      </section>
    }
  `,
  styles: [`
    :host { display: block; min-width: 0; }
    .back-link { display: inline-block; margin: 0 0 1rem; color: var(--accent-strong); }
    .purchase-summary, .item-section { min-width: 0; padding: 1rem; border: 1px solid var(--border-color); border-radius: 0.45rem; background: #fff; }
    .purchase-summary { margin-bottom: 1rem; }
    .summary-heading { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 0.75rem; }
    h2 { margin: 0 0 0.8rem; color: var(--primary-text); font-size: 1.05rem; }
    .summary-heading h2 { margin: 0; }
    .summary-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.8rem 1.4rem; margin: 0; }
    .summary-grid > div { min-width: 0; }
    dt { color: var(--secondary-text); font-size: 0.8rem; }
    dd { overflow-wrap: anywhere; margin: 0.2rem 0 0; color: var(--primary-text); }
    .identifier { display: block; color: var(--secondary-text); font-size: 0.78rem; }
    .total { font-weight: 600; }
    .status { padding: 0.25rem 0.5rem; border-radius: 0.4rem; background: var(--surface-soft); border: 1px solid var(--border-color); color: var(--secondary-text); font-size: 0.75rem; font-weight: 500; }
    .status.received { background: rgba(32, 170, 124, 0.12); color: var(--success); }
    .status.cancelled { background: rgba(214, 81, 81, 0.1); color: var(--danger); }
    .reference-warning { color: var(--secondary-text); }
    @media (max-width: 600px) { .summary-grid { grid-template-columns: minmax(0, 1fr); } }
  `],
})
export class PurchaseDetailsPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly purchaseService = inject(PurchaseService);
  private readonly companyService = inject(CompanyService);
  private readonly supplierService = inject(SupplierService);
  private readonly warehouseService = inject(WarehouseService);
  private readonly productService = inject(ProductService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly referenceWarning = signal<string | null>(null);
  readonly purchase = signal<PurchaseRecord | null>(null);
  readonly itemRows = signal<Record<string, unknown>[]>([]);
  readonly companyName = signal('');
  readonly supplierName = signal('');
  readonly warehouseName = signal('');

  readonly itemColumns: DataTableColumn[] = [
    { key: 'product', label: 'Product / SKU' },
    { key: 'quantity', label: 'Quantity' },
    { key: 'unitPrice', label: 'Unit Price' },
    { key: 'totalPrice', label: 'Line Total' },
  ];

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Purchase ID is missing from the route.');
      this.loading.set(false);
      return;
    }

    forkJoin({
      purchase: this.purchaseService.get(id),
      companies: this.companyService.list().pipe(catchError(() => {
        this.referenceWarning.set('Company names could not be resolved; showing available IDs instead.');
        return of([] as Company[]);
      })),
      suppliers: this.supplierService.list().pipe(catchError(() => {
        this.referenceWarning.set('Supplier names could not be resolved; showing available IDs instead.');
        return of([] as Supplier[]);
      })),
      warehouses: this.warehouseService.list().pipe(catchError(() => {
        this.referenceWarning.set('Warehouse names could not be resolved; showing available IDs instead.');
        return of([] as Warehouse[]);
      })),
      products: this.productService.list().pipe(catchError(() => {
        this.referenceWarning.set('Product names could not be resolved; showing product IDs instead.');
        return of([] as Product[]);
      })),
    }).pipe(take(1)).subscribe({
      next: ({ purchase, companies, suppliers, warehouses, products }) => {
        this.purchase.set(purchase);
        this.companyName.set(companies.find((item) => item.id === purchase.companyId)?.name ?? purchase.companyId);
        this.supplierName.set(suppliers.find((item) => item.id === purchase.supplierId)?.name ?? purchase.supplierId);
        this.warehouseName.set(warehouses.find((item) => item.id === purchase.warehouseId)?.name ?? purchase.warehouseId);
        this.itemRows.set(purchase.items.map((item) => {
          const product = products.find((candidate) => candidate.id === item.productId);
          return {
            product: product ? `${product.name} · ${product.sku}` : item.productId,
            quantity: item.quantity,
            unitPrice: item.unitPrice,
            totalPrice: item.totalPrice,
          };
        }));
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(error instanceof Error ? error.message : 'Unable to load purchase details.');
        this.loading.set(false);
      },
    });
  }
}