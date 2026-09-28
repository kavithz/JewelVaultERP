import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import {
  Company,
  CreateSaleItemRequest,
  CreateSaleRequest,
  Customer,
  Product,
  SaleRecord,
  SaleStatus,
  Warehouse,
} from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { CustomerService } from '../../core/services/customer.service';
import { ProductService } from '../../core/services/product.service';
import { SaleService } from '../../core/services/sale.service';
import { StockModuleListState } from '../../core/services/stock-module-list-state';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

type SaleItemForm = FormGroup<{
  productId: FormControl<string>;
  quantity: FormControl<number | null>;
  unitPrice: FormControl<number | null>;
}>;

@Component({
  selector: 'app-sales-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Sales"
      subtitle="Sales orders, totals, and fulfillment status."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Create sale"
      [createDisabled]="loading() || referenceError() !== null || companies().length === 0 || customers().length === 0 || warehouses().length === 0 || products().length === 0 || submitting()"
      [actions]="actions"
      [actionsDisabled]="submitting()"
      [busyRowId]="busyRowId()"
      [mutationError]="editorOpen() ? null : mutationError()"
      [successMessage]="successMessage()"
      [scopeNotice]="scopeNotice"
      [scopeError]="referenceError()"
      (createRequested)="openCreate()"
      (rowAction)="handleRowAction($event)"
      emptyTitle="No sales found."
      emptyMessage="Sale records are not available yet."
    >
      @if (editorOpen()) {
        <section master-data-editor class="sale-editor" aria-label="Create sale">
          <app-master-data-form-panel
            [form]="headerForm"
            [fields]="formFields"
            title="Create sale"
            submitLabel="Create draft sale"
            [showFooter]="false"
            [submitting]="submitting()"
            [error]="mutationError()"
          />
          <section class="items-section" aria-labelledby="sale-items-title">
            <div class="items-heading">
              <div>
                <h2 id="sale-items-title">Sale items</h2>
                <p>Subtotal and total are calculated by the backend when the sale is created.</p>
              </div>
              <button type="button" class="add-item" [disabled]="submitting()" (click)="addItem()">Add item</button>
            </div>
            @if (itemError()) { <p class="item-error" role="alert">{{ itemError() }}</p> }
            <div class="item-list">
              @for (item of items.controls; track item; let index = $index) {
                <div class="item-row" [formGroup]="item">
                  <div class="item-field">
                    <label [for]="'sale-product-' + index">Product</label>
                    <select [id]="'sale-product-' + index" formControlName="productId" required>
                      <option value="">Select product</option>
                      @for (product of products(); track product.id) {
                        <option [value]="product.id">{{ product.name }} · {{ product.sku }} · company {{ product.companyId.slice(0, 8) }}</option>
                      }
                    </select>
                    @if (item.controls.productId.touched && item.controls.productId.invalid) { <small class="item-error">Product is required.</small> }
                  </div>
                  <div class="item-field">
                    <label [for]="'sale-quantity-' + index">Quantity</label>
                    <input [id]="'sale-quantity-' + index" type="number" min="0.001" step="any" formControlName="quantity" required />
                    @if (item.controls.quantity.touched && item.controls.quantity.invalid) { <small class="item-error">Quantity must be at least 0.001.</small> }
                  </div>
                  <div class="item-field">
                    <label [for]="'sale-unit-price-' + index">Unit price</label>
                    <input [id]="'sale-unit-price-' + index" type="number" min="0" step="any" formControlName="unitPrice" required />
                    @if (item.controls.unitPrice.touched && item.controls.unitPrice.invalid) { <small class="item-error">Unit price cannot be negative.</small> }
                  </div>
                  <button type="button" class="remove-item" [disabled]="submitting() || items.length <= 1" [attr.aria-label]="'Remove sale item ' + (index + 1)" (click)="removeItem(index)">Remove</button>
                </div>
              }
            </div>
            <footer class="items-actions">
              <button type="button" class="secondary-button" [disabled]="submitting()" (click)="cancelEditor()">Cancel</button>
              <button type="button" class="primary-button" [disabled]="submitting()" (click)="save()">{{ submitting() ? 'Saving...' : 'Create draft sale' }}</button>
            </footer>
          </section>
        </section>
      }
    </app-master-data-list>
  `,
  styles: [`
    .sale-editor { display: block; margin-bottom: 1rem; }
    .items-section { margin-top: -0.5rem; padding: 0 1rem 1rem; border: 1px solid var(--border-color); border-top: 0; border-radius: 0 0 0.5rem 0.5rem; background: #fff; }
    .items-heading { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 0.75rem; }
    h2 { margin: 0; color: var(--primary-text); font-size: 1rem; }
    .items-heading p { margin: 0.3rem 0 0; color: var(--secondary-text); font-size: 0.85rem; }
    .add-item, .remove-item, .secondary-button, .primary-button { min-height: 2.4rem; padding: 0.45rem 0.7rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--accent-strong); font: inherit; cursor: pointer; }
    .item-list { display: grid; gap: 0.65rem; }
    .item-row { display: grid; grid-template-columns: minmax(12rem, 2fr) minmax(7rem, 1fr) minmax(7rem, 1fr) auto; align-items: start; gap: 0.75rem; }
    .item-field { display: grid; gap: 0.3rem; min-width: 0; }
    label { color: var(--primary-text); font-size: 0.82rem; font-weight: 500; }
    select, input { width: 100%; min-height: 2.5rem; box-sizing: border-box; padding: 0.55rem 0.65rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--primary-text); font: inherit; }
    button:focus-visible, input:focus-visible, select:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    button:disabled { opacity: 0.55; cursor: not-allowed; }
    .item-error { color: var(--danger); }
    .items-actions { display: flex; justify-content: flex-end; gap: 0.65rem; margin-top: 1rem; }
    .primary-button { border-color: var(--accent-strong); background: var(--accent-strong); color: #fff; font-weight: 600; }
    @media (max-width: 760px) { .item-row { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); } .remove-item { justify-self: start; } }
    @media (max-width: 460px) { .items-heading { align-items: stretch; flex-direction: column; } .item-row { grid-template-columns: minmax(0, 1fr); } .items-actions > button { flex: 1; } }
  `],
})
export class SalesPageComponent extends StockModuleListState<SaleRecord> {
  private readonly saleService = inject(SaleService);
  private readonly companyService = inject(CompanyService);
  private readonly customerService = inject(CustomerService);
  private readonly warehouseService = inject(WarehouseService);
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);
  readonly companies = signal<Company[]>([]);
  readonly customers = signal<Customer[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly products = signal<Product[]>([]);
  readonly referenceError = signal<string | null>(null);
  readonly editorOpen = signal(false);
  readonly itemError = signal<string | null>(null);
  readonly items = new FormArray<SaleItemForm>([]);

  readonly headerForm = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    customerId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    warehouseId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    saleNumber: new FormControl('', { nonNullable: true, validators: [notBlankValidator, Validators.maxLength(100)] }),
    saleDate: new FormControl('', { nonNullable: true }),
    taxAmount: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    discountAmount: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
  });

  readonly columns: DataTableColumn[] = [
    { key: 'saleNumber', label: 'Sale Number' },
    { key: 'customer', label: 'Customer' },
    { key: 'warehouse', label: 'Warehouse' },
    { key: 'saleDate', label: 'Sale Date' },
    { key: 'status', label: 'Status' },
    { key: 'subtotal', label: 'Subtotal' },
    { key: 'taxAmount', label: 'Tax' },
    { key: 'discountAmount', label: 'Discount' },
    { key: 'totalAmount', label: 'Total' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'createdAt', label: 'Created' },
  ];

  readonly actions: DataTableRowAction[] = [
    { key: 'view', label: 'View' },
    { key: 'complete', label: 'Complete', visibleWhen: (row) => row['status'] === 'DRAFT' },
    { key: 'cancel', label: 'Cancel sale', visibleWhen: (row) => row['status'] === 'DRAFT' },
  ];

  readonly scopeNotice = 'Sales list results are global. The backend validates customer, warehouse, and product company relationships when creating a sale.';
  readonly fields: MasterDataFormField[] = [
    { key: 'companyId', label: 'Company', type: 'select', required: true },
    { key: 'customerId', label: 'Customer', type: 'select', required: true },
    { key: 'warehouseId', label: 'Warehouse', type: 'select', required: true },
    { key: 'saleNumber', label: 'Sale number', type: 'text', required: true, maxLength: 100 },
    { key: 'saleDate', label: 'Sale date and time', type: 'datetime-local' },
    { key: 'taxAmount', label: 'Tax amount', type: 'number', required: true, min: 0 },
    { key: 'discountAmount', label: 'Discount amount', type: 'number', required: true, min: 0 },
  ];

  get formFields(): MasterDataFormField[] {
    return this.fields.map((field) => {
      if (field.key === 'companyId') {
        return { ...field, options: this.companies().map((company) => ({ value: company.id, label: `${company.name} (${company.id.slice(0, 8)})` })) };
      }
      if (field.key === 'customerId') {
        return { ...field, options: this.customers().map((customer) => ({ value: customer.id, label: `${customer.name} · ${customer.code} · company ${customer.companyId.slice(0, 8)}` })) };
      }
      if (field.key === 'warehouseId') {
        return { ...field, options: this.warehouses().map((warehouse) => ({ value: warehouse.id, label: `${warehouse.name} · ${warehouse.code} · company ${warehouse.companyId.slice(0, 8)}` })) };
      }
      return field;
    });
  }

  constructor() {
    super();
    this.items.push(this.createItemForm());
    this.load(
      forkJoin({
        sales: this.saleService.list(),
        companies: this.companyService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load companies.');
          return of([] as Company[]);
        })),
        customers: this.customerService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load customers.');
          return of([] as Customer[]);
        })),
        warehouses: this.warehouseService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load warehouses.');
          return of([] as Warehouse[]);
        })),
        products: this.productService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load products.');
          return of([] as Product[]);
        })),
      }).pipe(map(({ sales, companies, customers, warehouses, products }) => {
        this.companies.set(companies);
        this.customers.set(customers);
        this.warehouses.set(warehouses);
        this.products.set(products);
        return sales;
      })),
      (sale) => this.toRow(sale),
      'sales',
    );
  }

  addItem(): void {
    if (!this.submitting()) this.items.push(this.createItemForm());
  }

  removeItem(index: number): void {
    if (!this.submitting() && this.items.length > 1) this.items.removeAt(index);
  }

  openCreate(): void {
    if (this.submitting() || this.referenceError() || !this.companies().length || !this.customers().length || !this.warehouses().length || !this.products().length) return;
    this.mutationError.set(null);
    this.itemError.set(null);
    this.successMessage.set(null);
    this.headerForm.reset();
    this.items.clear();
    this.items.push(this.createItemForm());
    this.editorOpen.set(true);
  }

  handleRowAction(event: { action: string; row: Record<string, unknown> }): void {
    const sale = this.records().find((record) => record.id === String(event.row['id']));
    if (!sale) return;
    if (event.action === 'view') {
      void this.router.navigate(['/sales', sale.id]);
      return;
    }
    if (sale.status !== 'DRAFT') return;
    let status: SaleStatus;
    if (event.action === 'complete') {
      if (!window.confirm(`Complete sale ${sale.saleNumber}? This will deduct its items from inventory.`)) return;
      status = 'COMPLETED';
    } else if (event.action === 'cancel') {
      if (!window.confirm(`Cancel draft sale ${sale.saleNumber}?`)) return;
      status = 'CANCELLED';
    } else {
      return;
    }
    this.mutate(this.saleService.setStatus(sale.id, status), `Sale ${sale.saleNumber} ${status.toLowerCase()}.`, () => undefined, sale.id);
  }

  save(): void {
    if (this.submitting()) return;
    this.itemError.set(null);
    this.headerForm.markAllAsTouched();
    this.items.controls.forEach((item) => item.markAllAsTouched());
    if (!this.items.length) {
      this.itemError.set('At least one sale item is required.');
      return;
    }
    if (this.headerForm.invalid || this.items.invalid) return;
    const value = this.headerForm.getRawValue();
    const request: CreateSaleRequest = {
      companyId: value.companyId,
      customerId: value.customerId,
      warehouseId: value.warehouseId,
      saleNumber: value.saleNumber.trim(),
      ...(value.saleDate ? { saleDate: value.saleDate } : {}),
      taxAmount: Number(value.taxAmount),
      discountAmount: Number(value.discountAmount),
      items: this.items.getRawValue().map((item): CreateSaleItemRequest => ({
        productId: item.productId,
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
      })),
    };
    this.mutate(this.saleService.create(request), 'Draft sale created.', () => this.closeEditor());
  }

  cancelEditor(): void {
    this.mutationError.set(null);
    this.closeEditor();
  }

  private createItemForm(): SaleItemForm {
    return new FormGroup({
      productId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      quantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.001)] }),
      unitPrice: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    });
  }

  private closeEditor(): void {
    this.editorOpen.set(false);
    this.headerForm.reset();
    this.items.clear();
    this.items.push(this.createItemForm());
    this.itemError.set(null);
  }

  private toRow(sale: SaleRecord): Record<string, unknown> {
    const customer = this.customers().find((item) => item.id === sale.customerId);
    const warehouse = this.warehouses().find((item) => item.id === sale.warehouseId);
    return {
      id: sale.id,
      name: sale.saleNumber,
      saleNumber: sale.saleNumber,
      customer: customer?.name ?? sale.customerId,
      warehouse: warehouse?.name ?? sale.warehouseId,
      saleDate: sale.saleDate,
      status: sale.status,
      subtotal: sale.subtotal,
      taxAmount: sale.taxAmount,
      discountAmount: sale.discountAmount,
      totalAmount: sale.totalAmount,
      companyId: sale.companyId,
      createdAt: sale.createdAt,
    };
  }
}