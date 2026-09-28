import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import { Company, Product, PurchaseRecord, PurchaseStatus, Supplier, Warehouse } from '../../core/models/erp.models';
import { CompanyService } from '../../core/services/company.service';
import { ProductService } from '../../core/services/product.service';
import { PurchaseService } from '../../core/services/purchase.service';
import { StockModuleListState } from '../../core/services/stock-module-list-state';
import { SupplierService } from '../../core/services/supplier.service';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

@Component({
  selector: 'app-purchases-page',
  standalone: true,
  imports: [MasterDataListComponent],
  template: `
    <app-master-data-list
      title="Purchases"
      subtitle="Supplier purchases, totals, and receiving status."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Create purchase"
      [createDisabled]="loading() || submitting() || referenceError() !== null || companies().length === 0 || suppliers().length === 0 || warehouses().length === 0 || products().length === 0"
      [actions]="actions"
      [actionsDisabled]="submitting()"
      [busyRowId]="busyRowId()"
      [scopeNotice]="scopeNotice"
      [scopeError]="referenceError()"
      [mutationError]="mutationError()"
      [successMessage]="successMessage()"
      (createRequested)="openCreate()"
      (rowAction)="handleRowAction($event)"
      emptyTitle="No purchases found."
      emptyMessage="Purchase records are not available yet."
    />
  `,
})
export class PurchasesPageComponent extends StockModuleListState<PurchaseRecord> {
  private readonly purchaseService = inject(PurchaseService);
  private readonly companyService = inject(CompanyService);
  private readonly supplierService = inject(SupplierService);
  private readonly warehouseService = inject(WarehouseService);
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);

  readonly companies = signal<Company[]>([]);
  readonly suppliers = signal<Supplier[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly products = signal<Product[]>([]);
  readonly referenceError = signal<string | null>(null);

  readonly columns: DataTableColumn[] = [
    { key: 'purchaseNumber', label: 'Purchase Number' },
    { key: 'supplier', label: 'Supplier' },
    { key: 'purchaseDate', label: 'Purchase Date' },
    { key: 'status', label: 'Status' },
    { key: 'subtotal', label: 'Subtotal' },
    { key: 'taxAmount', label: 'Tax' },
    { key: 'discountAmount', label: 'Discount' },
    { key: 'totalAmount', label: 'Total' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'warehouse', label: 'Warehouse' },
    { key: 'createdAt', label: 'Created' },
  ];

  readonly actions: DataTableRowAction[] = [
    { key: 'view', label: 'View' },
    { key: 'receive', label: 'Receive', visibleWhen: (row) => row['status'] === 'DRAFT' },
    { key: 'cancel', label: 'Cancel draft', visibleWhen: (row) => row['status'] === 'DRAFT' },
  ];

  readonly scopeNotice = 'Purchase list results are global. The backend checks company ownership when creating purchases; selectors do not provide authorization filtering.';

  constructor() {
    super();
    this.load(
      forkJoin({
        purchases: this.purchaseService.list(),
        companies: this.companyService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load companies.');
          return of([] as Company[]);
        })),
        suppliers: this.supplierService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load suppliers.');
          return of([] as Supplier[]);
        })),
        warehouses: this.warehouseService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load warehouses.');
          return of([] as Warehouse[]);
        })),
        products: this.productService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load products.');
          return of([] as Product[]);
        })),
      }).pipe(map(({ purchases, companies, suppliers, warehouses, products }) => {
        this.companies.set(companies);
        this.suppliers.set(suppliers);
        this.warehouses.set(warehouses);
        this.products.set(products);
        return purchases;
      })),
      (purchase) => this.toRow(purchase),
      'purchases',
    );
  }

  openCreate(): void {
    if (this.submitting() || this.referenceError() || !this.companies().length || !this.suppliers().length || !this.warehouses().length || !this.products().length) return;
    void this.router.navigate(['/purchases/new']);
  }

  handleRowAction(event: { action: string; row: Record<string, unknown> }): void {
    const purchase = this.records().find((record) => record.id === String(event.row['id']));
    if (!purchase) return;
    if (event.action === 'view') {
      void this.router.navigate(['/purchases', purchase.id]);
      return;
    }
    if (purchase.status !== 'DRAFT') return;

    let status: PurchaseStatus;
    if (event.action === 'receive') {
      if (!window.confirm(`Receive purchase ${purchase.purchaseNumber}? This will create PURCHASE stock movements.`)) return;
      status = 'RECEIVED';
    } else if (event.action === 'cancel') {
      if (!window.confirm(`Cancel draft purchase ${purchase.purchaseNumber}?`)) return;
      status = 'CANCELLED';
    } else {
      return;
    }

    this.mutate(
      this.purchaseService.setStatus(purchase.id, status),
      `Purchase ${purchase.purchaseNumber} marked ${status.toLowerCase()}.`,
      () => undefined,
      purchase.id,
    );
  }

  private toRow(purchase: PurchaseRecord): Record<string, unknown> {
    const supplier = this.suppliers().find((item) => item.id === purchase.supplierId);
    const company = this.companies().find((item) => item.id === purchase.companyId);
    const warehouse = this.warehouses().find((item) => item.id === purchase.warehouseId);
    return {
      id: purchase.id,
      name: purchase.purchaseNumber,
      purchaseNumber: purchase.purchaseNumber,
      supplier: supplier?.name ?? purchase.supplierId,
      purchaseDate: purchase.purchaseDate,
      status: purchase.status,
      subtotal: purchase.subtotal,
      taxAmount: purchase.taxAmount,
      discountAmount: purchase.discountAmount,
      totalAmount: purchase.totalAmount,
      companyId: company ? `${company.name} · ${purchase.companyId}` : purchase.companyId,
      warehouse: warehouse?.name ?? purchase.warehouseId,
      createdAt: purchase.createdAt,
    };
  }
}