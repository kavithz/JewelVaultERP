import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, map, of } from 'rxjs';
import {
  AdjustmentType,
  Company,
  CreateStockAdjustmentItemRequest,
  CreateStockAdjustmentRequest,
  Product,
  SelectOption,
  StockAdjustmentRecord,
  StockAdjustmentStatus,
  Warehouse,
} from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { ProductService } from '../../core/services/product.service';
import { StockAdjustmentService } from '../../core/services/stock-adjustment.service';
import { StockModuleListState } from '../../core/services/stock-module-list-state';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

type AdjustmentItemForm = FormGroup<{
  productId: FormControl<string>;
  quantity: FormControl<number | null>;
  adjustmentType: FormControl<AdjustmentType | ''>;
}>;

@Component({
  selector: 'app-stock-adjustments-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Stock Adjustments"
      subtitle="Draft stock corrections and their processing status."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Create adjustment"
      [createDisabled]="loading() || referenceError() !== null || companies().length === 0 || warehouses().length === 0 || products().length === 0 || submitting()"
      [actions]="actions"
      [actionsDisabled]="submitting()"
      [busyRowId]="busyRowId()"
      [mutationError]="editorOpen() ? null : mutationError()"
      [successMessage]="successMessage()"
      [scopeNotice]="scopeNotice"
      [scopeError]="referenceError()"
      (createRequested)="openCreate()"
      (rowAction)="handleRowAction($event)"
      emptyTitle="No stock adjustments found."
      emptyMessage="Adjustment records are not available yet."
    >
      @if (editorOpen()) {
        <section master-data-editor class="adjustment-editor" aria-label="Create stock adjustment">
          <app-master-data-form-panel
            [form]="headerForm"
            [fields]="formFields"
            title="Create stock adjustment"
            submitLabel="Create draft"
            [submitting]="submitting()"
            [error]="mutationError()"
            (submitted)="save()"
            (cancelled)="cancelEditor()"
            (cancelled)="cancelEditor()"
          />
          <section class="items-section" aria-labelledby="adjustment-items-title">
            <div class="items-heading">
              <div>
                <h3 id="adjustment-items-title">Adjustment items</h3>
                <p>At least one item is required. Inventory changes occur when the draft is completed.</p>
              </div>
              <button type="button" class="add-item" [disabled]="submitting()" (click)="addItem()">Add item</button>
            </div>
            @if (itemError()) { <p class="item-error" role="alert">{{ itemError() }}</p> }
            <div class="item-list" aria-live="polite">
              @for (item of items.controls; track item; let index = $index) {
                <div class="item-row" [formGroup]="item">
                  <div class="item-field">
                    <label [for]="'adjustment-product-' + index">Product</label>
                    <select [id]="'adjustment-product-' + index" formControlName="productId" required>
                      <option value="">Select product</option>
                      @for (option of productOptions; track option.value) {
                        <option [value]="option.value">{{ option.label }}</option>
                      }
                    </select>
                    @if (item.controls.productId.touched && item.controls.productId.invalid) {
                      <small class="item-error">Product is required.</small>
                    }
                  </div>
                  <div class="item-field">
                    <label [for]="'adjustment-quantity-' + index">Quantity</label>
                    <input [id]="'adjustment-quantity-' + index" type="number" min="0.001" step="any" formControlName="quantity" required />
                    @if (item.controls.quantity.touched && item.controls.quantity.invalid) {
                      <small class="item-error">Quantity must be at least 0.001.</small>
                    }
                  </div>
                  <div class="item-field">
                    <label [for]="'adjustment-type-' + index">Adjustment type</label>
                    <select [id]="'adjustment-type-' + index" formControlName="adjustmentType" required>
                      <option value="">Select type</option>
                      <option value="ADJUSTMENT_IN">Adjustment in</option>
                      <option value="ADJUSTMENT_OUT">Adjustment out</option>
                    </select>
                    @if (item.controls.adjustmentType.touched && item.controls.adjustmentType.invalid) {
                      <small class="item-error">Adjustment type is required.</small>
                    }
                  </div>
                  <button type="button" class="remove-item" [disabled]="submitting() || items.length <= 1" [attr.aria-label]="'Remove adjustment item ' + (index + 1)" (click)="removeItem(index)">Remove</button>
                </div>
              }
            </div>
          </section>
        </section>
      }
    </app-master-data-list>
  `,
  styles: [`
    .adjustment-editor { display: block; margin-bottom: 1rem; }
    .items-section { margin-top: -0.5rem; padding: 0 1rem 1rem; border: 1px solid var(--border-color); border-top: 0; border-radius: 0 0 0.5rem 0.5rem; background: #fff; }
    .items-heading { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 0.75rem; }
    h3 { margin: 0; color: var(--primary-text); font-size: 1rem; }
    .items-heading p { margin: 0.3rem 0 0; color: var(--secondary-text); font-size: 0.85rem; }
    .add-item, .remove-item, .items-submit button { min-height: 2.4rem; padding: 0.45rem 0.7rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--accent-strong); font: inherit; cursor: pointer; }
    .item-list { display: grid; gap: 0.65rem; }
    .item-row { display: grid; grid-template-columns: minmax(12rem, 2fr) minmax(7rem, 1fr) minmax(10rem, 1fr) auto; align-items: start; gap: 0.75rem; }
    .item-field { display: grid; gap: 0.3rem; min-width: 0; }
    label { color: var(--primary-text); font-size: 0.82rem; font-weight: 500; }
    select, input { width: 100%; min-height: 2.5rem; box-sizing: border-box; padding: 0.55rem 0.65rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--primary-text); font: inherit; }
    button:focus-visible, input:focus-visible, select:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    button:disabled { opacity: 0.55; cursor: not-allowed; }
    .item-error { color: var(--danger); }
    @media (max-width: 760px) { .item-row { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); } .remove-item { justify-self: start; } }
    @media (max-width: 460px) { .items-heading { align-items: stretch; flex-direction: column; } .item-row { grid-template-columns: minmax(0, 1fr); } .items-submit button { flex: 1; } }
  `],
})
export class StockAdjustmentsPageComponent extends StockModuleListState<StockAdjustmentRecord> {
  private readonly adjustmentService = inject(StockAdjustmentService);
  private readonly companyService = inject(CompanyService);
  private readonly warehouseService = inject(WarehouseService);
  private readonly productService = inject(ProductService);
  readonly companies = signal<Company[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly products = signal<Product[]>([]);
  readonly referenceError = signal<string | null>(null);
  readonly editorOpen = signal(false);
  readonly itemError = signal<string | null>(null);

  readonly items = new FormArray<AdjustmentItemForm>([]);
  readonly headerForm = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    warehouseId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    adjustmentNumber: new FormControl('', { nonNullable: true, validators: [notBlankValidator, Validators.maxLength(100)] }),
    notes: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
  });

  readonly columns: DataTableColumn[] = [
    { key: 'adjustmentNumber', label: 'Adjustment' },
    { key: 'adjustmentDate', label: 'Date' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'warehouse', label: 'Warehouse' },
    { key: 'itemsSummary', label: 'Items' },
    { key: 'status', label: 'Status' },
    { key: 'notes', label: 'Notes' },
  ];

  readonly actions: DataTableRowAction[] = [
    { key: 'complete', label: 'Complete', visibleWhen: (row) => row['status'] === 'DRAFT' },
    { key: 'cancel', label: 'Cancel draft', visibleWhen: (row) => row['status'] === 'DRAFT' },
  ];

  readonly scopeNotice = 'Adjustment lists are global. The backend validates that the selected warehouse and each selected product belong to the chosen company.';
  readonly fields: MasterDataFormField[] = [
    { key: 'companyId', label: 'Company', type: 'select', required: true },
    { key: 'warehouseId', label: 'Warehouse', type: 'select', required: true },
    { key: 'adjustmentNumber', label: 'Adjustment number', type: 'text', required: true, maxLength: 100 },
    { key: 'notes', label: 'Notes', type: 'textarea', maxLength: 500 },
  ];

  get productOptions(): SelectOption[] {
    return this.products().map((product) => ({
      value: product.id,
      label: `${product.name} · ${product.sku} · company ${product.companyId.slice(0, 8)}`,
    }));
  }

  constructor() {
    super();
    this.items.push(this.createItemForm());
    this.load(
      forkJoin({
        adjustments: this.adjustmentService.list(),
        companies: this.companyService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load companies.');
          return of([] as Company[]);
        })),
        warehouses: this.warehouseService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load warehouses.');
          return of([] as Warehouse[]);
        })),
        products: this.productService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load products.');
          return of([] as Product[]);
        })),
      }).pipe(map(({ adjustments, companies, warehouses, products }) => {
        this.companies.set(companies);
        this.warehouses.set(warehouses);
        this.products.set(products);
        return adjustments;
      })),
      (adjustment) => this.toRow(adjustment),
      'stock adjustments',
    );
  }

  get formFields(): MasterDataFormField[] {
    return this.fields.map((field) => {
      if (field.key === 'companyId') {
        return { ...field, options: this.companies().map((company) => this.companyOption(company)) };
      }
      if (field.key === 'warehouseId') {
        return { ...field, options: this.warehouses().map((warehouse) => ({
          value: warehouse.id,
          label: `${warehouse.name} · ${warehouse.code} · company ${warehouse.companyId.slice(0, 8)}`,
        })) };
      }
      return field;
    });
  }

  openCreate(): void {
    if (this.submitting() || this.referenceError() || !this.companies().length || !this.warehouses().length || !this.products().length) return;
    this.mutationError.set(null);
    this.itemError.set(null);
    this.successMessage.set(null);
    this.headerForm.reset();
    this.items.clear();
    this.items.push(this.createItemForm());
    this.editorOpen.set(true);
  }

  addItem(): void {
    if (!this.submitting()) this.items.push(this.createItemForm());
  }

  removeItem(index: number): void {
    if (!this.submitting() && this.items.length > 1) this.items.removeAt(index);
  }

  handleRowAction(event: { action: string; row: Record<string, unknown> }): void {
    const adjustment = this.records().find((record) => record.id === String(event.row['id']));
    if (!adjustment || adjustment.status !== 'DRAFT') return;
    let status: StockAdjustmentStatus;
    if (event.action === 'complete') {
      if (!window.confirm(`Complete adjustment ${adjustment.adjustmentNumber}? This applies its stock changes.`)) return;
      status = 'COMPLETED';
    } else if (event.action === 'cancel') {
      if (!window.confirm(`Cancel draft adjustment ${adjustment.adjustmentNumber}?`)) return;
      status = 'CANCELLED';
    } else {
      return;
    }
    this.mutate(this.adjustmentService.setStatus(adjustment.id, status), `Adjustment ${adjustment.adjustmentNumber} ${status.toLowerCase()}.`, () => undefined, adjustment.id);
  }

  save(): void {
    if (this.submitting()) return;
    this.itemError.set(null);
    this.headerForm.markAllAsTouched();
    this.items.controls.forEach((item) => item.markAllAsTouched());
    if (!this.items.length) {
      this.itemError.set('At least one adjustment item is required.');
      return;
    }
    if (this.headerForm.invalid || this.items.invalid) return;

    const header = this.headerForm.getRawValue();
    const request: CreateStockAdjustmentRequest = {
      companyId: header.companyId,
      warehouseId: header.warehouseId,
      adjustmentNumber: header.adjustmentNumber.trim(),
      notes: header.notes.trim() || null,
      items: this.items.getRawValue().map((item): CreateStockAdjustmentItemRequest => ({
        productId: item.productId,
        quantity: Number(item.quantity),
        adjustmentType: item.adjustmentType as AdjustmentType,
      })),
    };
    this.mutate(this.adjustmentService.create(request), 'Stock adjustment draft created.', () => this.closeEditor());
  }

  cancelEditor(): void {
    this.mutationError.set(null);
    this.closeEditor();
  }

  private createItemForm(): AdjustmentItemForm {
    return new FormGroup({
      productId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      quantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.001)] }),
      adjustmentType: new FormControl<AdjustmentType | ''>('', { nonNullable: true, validators: [Validators.required] }),
    });
  }

  private companyOption(company: Company): SelectOption {
    return { value: company.id, label: `${company.name} (${company.id.slice(0, 8)})` };
  }

  private closeEditor(): void {
    this.editorOpen.set(false);
    this.headerForm.reset();
    this.items.clear();
    this.items.push(this.createItemForm());
    this.itemError.set(null);
  }

  private toRow(adjustment: StockAdjustmentRecord): Record<string, unknown> {
    const warehouse = this.warehouses().find((item) => item.id === adjustment.warehouseId);
    const itemSummary = adjustment.items.map((item) => {
      const product = this.products().find((candidate) => candidate.id === item.productId);
      const name = product ? `${product.name} (${product.sku})` : item.productId;
      return `${name}: ${item.quantity} ${item.adjustmentType.replaceAll('_', ' ')}`;
    }).join('; ');
    return {
      id: adjustment.id,
      adjustmentNumber: adjustment.adjustmentNumber,
      adjustmentDate: adjustment.adjustmentDate,
      companyId: adjustment.companyId,
      warehouse: warehouse?.name ?? adjustment.warehouseId,
      itemsSummary: itemSummary,
      status: adjustment.status,
      notes: adjustment.notes,
    };
  }
}