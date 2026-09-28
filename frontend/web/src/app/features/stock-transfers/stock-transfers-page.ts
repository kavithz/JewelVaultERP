import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import {
  Company,
  CreateStockTransferItemRequest,
  CreateStockTransferRequest,
  Product,
  StockTransferRecord,
  StockTransferStatus,
  Warehouse,
} from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { ProductService } from '../../core/services/product.service';
import { StockModuleListState } from '../../core/services/stock-module-list-state';
import { StockTransferService } from '../../core/services/stock-transfer.service';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

type TransferItemForm = FormGroup<{
  productId: FormControl<string>;
  quantity: FormControl<number | null>;
}>;

const differentWarehousesValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const sourceId = control.get('sourceWarehouseId')?.value;
  const destinationId = control.get('destinationWarehouseId')?.value;
  return sourceId && destinationId && sourceId === destinationId ? { sameWarehouses: true } : null;
};

@Component({
  selector: 'app-stock-transfers-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Stock Transfers"
      subtitle="Transfers between warehouses and their processing status."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Create transfer"
      [createDisabled]="loading() || submitting() || referenceError() !== null || companies().length === 0 || warehouses().length < 2 || products().length === 0"
      [actions]="actions"
      [actionsDisabled]="submitting()"
      [busyRowId]="busyRowId()"
      [scopeNotice]="scopeNotice"
      [scopeError]="referenceError()"
      [mutationError]="editorOpen() ? null : mutationError()"
      [successMessage]="successMessage()"
      (createRequested)="openCreate()"
      (rowAction)="handleRowAction($event)"
      emptyTitle="No stock transfers found."
      emptyMessage="Transfer records are not available yet."
    >
      @if (editorOpen()) {
        <section master-data-editor class="transfer-editor" aria-label="Create stock transfer">
          <app-master-data-form-panel
            [form]="headerForm"
            [fields]="fields"
            title="Create stock transfer"
            submitLabel="Create draft"
            [showFooter]="false"
            [submitting]="submitting()"
            [error]="mutationError()"
          />
          @if (headerForm.touched && headerForm.hasError('sameWarehouses')) {
            <p class="field-error" role="alert">Source and destination warehouses must be different.</p>
          }
          <section class="items-section" aria-labelledby="transfer-items-heading">
            <div class="items-heading">
              <div>
                <h2 id="transfer-items-heading">Transfer items</h2>
                <p>Available quantity is checked by the backend when creating and completing the transfer.</p>
              </div>
              <button type="button" class="add-item" [disabled]="submitting()" (click)="addItem()">Add item</button>
            </div>
            @if (itemError()) { <p class="field-error" role="alert">{{ itemError() }}</p> }
            <div class="item-list">
              @for (item of items.controls; track item; let index = $index) {
                <div class="item-row" [formGroup]="item">
                  <div class="item-field">
                    <label [for]="'transfer-product-' + index">Product</label>
                    <select [id]="'transfer-product-' + index" formControlName="productId" required>
                      <option value="">Select product</option>
                      @for (product of products(); track product.id) {
                        <option [value]="product.id">{{ product.name }} · {{ product.sku }} · company {{ product.companyId.slice(0, 8) }}</option>
                      }
                    </select>
                    @if (item.controls.productId.touched && item.controls.productId.invalid) { <small class="field-error">Product is required.</small> }
                  </div>
                  <div class="item-field">
                    <label [for]="'transfer-quantity-' + index">Quantity</label>
                    <input [id]="'transfer-quantity-' + index" type="number" min="0.001" step="any" formControlName="quantity" required />
                    @if (item.controls.quantity.touched && item.controls.quantity.invalid) { <small class="field-error">Quantity must be at least 0.001.</small> }
                  </div>
                  <button type="button" class="remove-item" [disabled]="submitting() || items.length <= 1" [attr.aria-label]="'Remove transfer item ' + (index + 1)" (click)="removeItem(index)">Remove</button>
                </div>
              }
            </div>
            <footer class="items-actions">
              <button type="button" [disabled]="submitting()" (click)="cancelEditor()">Cancel</button>
              <button type="button" class="primary-button" [disabled]="submitting()" (click)="save()">{{ submitting() ? 'Saving...' : 'Create draft' }}</button>
            </footer>
          </section>
        </section>
      }
    </app-master-data-list>
  `,
  styles: [`
    .transfer-editor { display: block; margin-bottom: 1rem; }
    .items-section { margin-top: -0.5rem; padding: 0 1rem 1rem; border: 1px solid var(--border-color); border-top: 0; border-radius: 0 0 0.5rem 0.5rem; background: #fff; }
    .items-heading { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 0.75rem; }
    h2 { margin: 0; color: var(--primary-text); font-size: 1rem; }
    .items-heading p { margin: 0.3rem 0 0; color: var(--secondary-text); font-size: 0.85rem; }
    .add-item, .remove-item, .items-actions button { min-height: 2.4rem; padding: 0.45rem 0.7rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--accent-strong); font: inherit; cursor: pointer; }
    .item-list { display: grid; gap: 0.65rem; }
    .item-row { display: grid; grid-template-columns: minmax(12rem, 2fr) minmax(7rem, 1fr) auto; align-items: start; gap: 0.75rem; }
    .item-field { display: grid; gap: 0.3rem; min-width: 0; }
    label { color: var(--primary-text); font-size: 0.82rem; font-weight: 500; }
    select, input { width: 100%; min-height: 2.5rem; box-sizing: border-box; padding: 0.55rem 0.65rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--primary-text); font: inherit; }
    button:focus-visible, input:focus-visible, select:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    button:disabled { opacity: 0.55; cursor: not-allowed; }
    .field-error { color: var(--danger); }
    .items-actions { display: flex; justify-content: flex-end; gap: 0.65rem; margin-top: 1rem; }
    .items-actions .primary-button { border-color: var(--accent-strong); background: var(--accent-strong); color: #fff; font-weight: 600; }
    @media (max-width: 620px) { .items-heading { align-items: stretch; flex-direction: column; } .item-row { grid-template-columns: minmax(0, 1fr); } .remove-item { justify-self: start; } .items-actions > button { flex: 1; } }
  `],
})
export class StockTransfersPageComponent extends StockModuleListState<StockTransferRecord> {
  private readonly transferService = inject(StockTransferService);
  private readonly companyService = inject(CompanyService);
  private readonly warehouseService = inject(WarehouseService);
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);
  readonly companies = signal<Company[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly products = signal<Product[]>([]);
  readonly referenceError = signal<string | null>(null);
  readonly editorOpen = signal(false);
  readonly itemError = signal<string | null>(null);
  readonly items = new FormArray<TransferItemForm>([]);

  readonly headerForm = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    sourceWarehouseId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    destinationWarehouseId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    transferNumber: new FormControl('', { nonNullable: true, validators: [notBlankValidator, Validators.maxLength(100)] }),
    transferDate: new FormControl('', { nonNullable: true }),
    notes: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
  }, { validators: [differentWarehousesValidator] });

  readonly columns: DataTableColumn[] = [
    { key: 'transferNumber', label: 'Transfer Number' },
    { key: 'transferDate', label: 'Transfer Date' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'sourceWarehouse', label: 'Source Warehouse' },
    { key: 'destinationWarehouse', label: 'Destination Warehouse' },
    { key: 'itemsSummary', label: 'Items' },
    { key: 'status', label: 'Status' },
    { key: 'notes', label: 'Notes' },
  ];

  readonly actions: DataTableRowAction[] = [
    { key: 'view', label: 'View' },
    { key: 'complete', label: 'Complete', visibleWhen: (row) => row['status'] === 'DRAFT' },
    { key: 'cancel', label: 'Cancel transfer', visibleWhen: (row) => row['status'] === 'DRAFT' },
  ];

  readonly scopeNotice = 'Transfer lists are global. The backend validates company ownership and stock availability; these selectors do not enforce authorization.';

  get fields(): MasterDataFormField[] {
    return [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.companies().map((company) => ({ value: company.id, label: `${company.name} (${company.id.slice(0, 8)})` })) },
      { key: 'sourceWarehouseId', label: 'Source warehouse', type: 'select', required: true, options: this.warehouseOptions },
      { key: 'destinationWarehouseId', label: 'Destination warehouse', type: 'select', required: true, options: this.warehouseOptions },
      { key: 'transferNumber', label: 'Transfer number', type: 'text', required: true, maxLength: 100 },
      { key: 'transferDate', label: 'Transfer date and time', type: 'datetime-local' },
      { key: 'notes', label: 'Notes', type: 'textarea', maxLength: 500 },
    ];
  }

  private get warehouseOptions(): { value: string; label: string }[] {
    return this.warehouses().map((warehouse) => ({
      value: warehouse.id,
      label: `${warehouse.name} · ${warehouse.code} · company ${warehouse.companyId.slice(0, 8)}`,
    }));
  }

  constructor() {
    super();
    this.items.push(this.createItemForm());
    this.load(
      forkJoin({
        transfers: this.transferService.list(),
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
      }).pipe(map(({ transfers, companies, warehouses, products }) => {
        this.companies.set(companies);
        this.warehouses.set(warehouses);
        this.products.set(products);
        return transfers;
      })),
      (transfer) => this.toRow(transfer),
      'stock transfers',
    );
  }

  get productOptions(): { value: string; label: string }[] {
    return this.products().map((product) => ({
      value: product.id,
      label: `${product.name} · ${product.sku} · company ${product.companyId.slice(0, 8)}`,
    }));
  }

  addItem(): void {
    if (!this.submitting()) this.items.push(this.createItemForm());
  }

  removeItem(index: number): void {
    if (!this.submitting() && this.items.length > 1) this.items.removeAt(index);
  }

  openCreate(): void {
    if (this.submitting() || this.referenceError() || !this.companies().length || this.warehouses().length < 2 || !this.products().length) return;
    this.mutationError.set(null);
    this.itemError.set(null);
    this.successMessage.set(null);
    this.headerForm.reset();
    this.items.clear();
    this.items.push(this.createItemForm());
    this.editorOpen.set(true);
  }

  handleRowAction(event: { action: string; row: Record<string, unknown> }): void {
    const transfer = this.records().find((record) => record.id === String(event.row['id']));
    if (!transfer) return;
    if (event.action === 'view') {
      void this.router.navigate(['/stock-transfers', transfer.id]);
      return;
    }
    if (transfer.status !== 'DRAFT') return;
    let status: StockTransferStatus;
    if (event.action === 'complete') {
      if (!window.confirm(`Complete transfer ${transfer.transferNumber}? This will move the listed quantities between warehouses.`)) return;
      status = 'COMPLETED';
    } else if (event.action === 'cancel') {
      if (!window.confirm(`Cancel draft transfer ${transfer.transferNumber}?`)) return;
      status = 'CANCELLED';
    } else {
      return;
    }
    this.mutate(this.transferService.setStatus(transfer.id, status), `Transfer ${transfer.transferNumber} ${status.toLowerCase()}.`, () => undefined, transfer.id);
  }

  save(): void {
    if (this.submitting()) return;
    this.itemError.set(null);
    this.headerForm.markAllAsTouched();
    this.items.controls.forEach((item) => item.markAllAsTouched());
    if (this.headerForm.hasError('sameWarehouses')) {
      this.mutationError.set('Source and destination warehouses must be different.');
      return;
    }
    if (!this.items.length) {
      this.itemError.set('At least one transfer item is required.');
      return;
    }
    if (this.headerForm.invalid || this.items.invalid) return;

    const header = this.headerForm.getRawValue();
    const request: CreateStockTransferRequest = {
      companyId: header.companyId,
      sourceWarehouseId: header.sourceWarehouseId,
      destinationWarehouseId: header.destinationWarehouseId,
      transferNumber: header.transferNumber.trim(),
      ...(header.transferDate ? { transferDate: header.transferDate } : {}),
      notes: header.notes.trim() || null,
      items: this.items.getRawValue().map((item): CreateStockTransferItemRequest => ({
        productId: item.productId,
        quantity: Number(item.quantity),
      })),
    };
    this.mutate(this.transferService.create(request), 'Draft transfer created.', () => this.closeEditor());
  }

  cancelEditor(): void {
    this.mutationError.set(null);
    this.closeEditor();
  }

  private createItemForm(): TransferItemForm {
    return new FormGroup({
      productId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      quantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.001)] }),
    });
  }

  private closeEditor(): void {
    this.editorOpen.set(false);
    this.headerForm.reset();
    this.items.clear();
    this.items.push(this.createItemForm());
    this.itemError.set(null);
  }

  private toRow(transfer: StockTransferRecord): Record<string, unknown> {
    const source = this.warehouses().find((warehouse) => warehouse.id === transfer.sourceWarehouseId);
    const destination = this.warehouses().find((warehouse) => warehouse.id === transfer.destinationWarehouseId);
    const itemsSummary = transfer.items.map((item) => {
      const product = this.products().find((candidate) => candidate.id === item.productId);
      return `${product ? `${product.name} (${product.sku})` : item.productId}: ${item.quantity}`;
    }).join('; ');
    return {
      id: transfer.id,
      name: transfer.transferNumber,
      transferNumber: transfer.transferNumber,
      transferDate: transfer.transferDate,
      companyId: transfer.companyId,
      sourceWarehouse: source?.name ?? transfer.sourceWarehouseId,
      destinationWarehouse: destination?.name ?? transfer.destinationWarehouseId,
      itemsSummary,
      status: transfer.status,
      notes: transfer.notes,
    };
  }
}