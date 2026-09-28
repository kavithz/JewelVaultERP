import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, of, take } from 'rxjs';
import { Company, Customer, Product, SaleRecord, Warehouse } from '../../core/models/erp.models';
import { CompanyService } from '../../core/services/company.service';
import { CustomerService } from '../../core/services/customer.service';
import { ProductService } from '../../core/services/product.service';
import { SaleService } from '../../core/services/sale.service';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableComponent } from '../../shared/components/data-table/data-table';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state';
import { LoadingStateComponent } from '../../shared/components/loading-state/loading-state';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-sale-details-page',
  standalone: true,
  imports: [CommonModule, RouterLink, PageHeaderComponent, DataTableComponent, LoadingStateComponent, ErrorStateComponent],
  template: `
    <app-page-header [title]="sale() ? 'Sale ' + sale()!.saleNumber : 'Sale details'" subtitle="Sales record and line items." />
    <a class="back-link" routerLink="/sales">Back to Sales</a>

    @if (loading()) {
      <app-loading-state />
    } @else if (error()) {
      <app-error-state [message]="error()!" />
    } @else if (sale(); as record) {
      @if (referenceWarning()) {
        <p class="reference-warning" role="status">{{ referenceWarning() }}</p>
      }
      <section class="sale-summary" aria-labelledby="sale-summary-heading">
        <div class="summary-heading">
          <h2 id="sale-summary-heading">Sale summary</h2>
          <span class="status" [class.completed]="record.status === 'COMPLETED'" [class.cancelled]="record.status === 'CANCELLED'">{{ record.status }}</span>
        </div>
        <dl class="summary-grid">
          <div><dt>Sale number</dt><dd>{{ record.saleNumber }}</dd></div>
          <div><dt>Sale date</dt><dd>{{ record.saleDate }}</dd></div>
          <div><dt>Company</dt><dd>{{ companyName() }} <span class="identifier">{{ record.companyId }}</span></dd></div>
          <div><dt>Customer</dt><dd>{{ customerName() }} <span class="identifier">{{ record.customerId }}</span></dd></div>
          <div><dt>Warehouse</dt><dd>{{ warehouseName() }} <span class="identifier">{{ record.warehouseId }}</span></dd></div>
          <div><dt>Created</dt><dd>{{ record.createdAt }}</dd></div>
          <div><dt>Subtotal</dt><dd>{{ record.subtotal }}</dd></div>
          <div><dt>Tax</dt><dd>{{ record.taxAmount }}</dd></div>
          <div><dt>Discount</dt><dd>{{ record.discountAmount }}</dd></div>
          <div><dt>Total</dt><dd class="total">{{ record.totalAmount }}</dd></div>
        </dl>
      </section>

      <section class="line-items" aria-labelledby="sale-items-heading">
        <h2 id="sale-items-heading">Line items</h2>
        <app-data-table
          [columns]="itemColumns"
          [rows]="itemRows()"
          [loading]="false"
          emptyTitle="No sale items found."
          emptyMessage="This sale response contains no line items."
        />
      </section>
    }
  `,
  styles: [`
    :host { display: block; min-width: 0; }
    .back-link { display: inline-block; margin: 0 0 1rem; color: var(--accent-strong); }
    .sale-summary, .line-items { min-width: 0; padding: 1rem; border: 1px solid var(--border-color); border-radius: 0.45rem; background: #fff; }
    .sale-summary { margin-bottom: 1rem; }
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
    .status.completed { background: rgba(32, 170, 124, 0.12); color: var(--success); }
    .status.cancelled { background: rgba(214, 81, 81, 0.1); color: var(--danger); }
    .reference-warning { color: var(--secondary-text); }
    @media (max-width: 600px) { .summary-grid { grid-template-columns: minmax(0, 1fr); } }
  `],
})
export class SaleDetailsPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly saleService = inject(SaleService);
  private readonly companyService = inject(CompanyService);
  private readonly customerService = inject(CustomerService);
  private readonly warehouseService = inject(WarehouseService);
  private readonly productService = inject(ProductService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly referenceWarning = signal<string | null>(null);
  readonly sale = signal<SaleRecord | null>(null);
  readonly itemRows = signal<Record<string, unknown>[]>([]);
  readonly companyName = signal('');
  readonly customerName = signal('');
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
      this.error.set('Sale ID is missing from the route.');
      this.loading.set(false);
      return;
    }

    forkJoin({
      sale: this.saleService.get(id),
      companies: this.companyService.list().pipe(catchError(() => {
        this.referenceWarning.set('Company names could not be resolved; showing available IDs instead.');
        return of([] as Company[]);
      })),
      customers: this.customerService.list().pipe(catchError(() => {
        this.referenceWarning.set('Customer names could not be resolved; showing available IDs instead.');
        return of([] as Customer[]);
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
      next: ({ sale, companies, customers, warehouses, products }) => {
        this.sale.set(sale);
        this.companyName.set(companies.find((company) => company.id === sale.companyId)?.name ?? sale.companyId);
        this.customerName.set(customers.find((customer) => customer.id === sale.customerId)?.name ?? sale.customerId);
        this.warehouseName.set(warehouses.find((warehouse) => warehouse.id === sale.warehouseId)?.name ?? sale.warehouseId);
        this.itemRows.set(sale.items.map((item) => {
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
        this.error.set(error instanceof Error ? error.message : 'Unable to load sale details.');
        this.loading.set(false);
      },
    });
  }
}