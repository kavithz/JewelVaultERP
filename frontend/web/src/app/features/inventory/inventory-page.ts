import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { catchError, forkJoin, map, of } from 'rxjs';
import {
  CreateInventoryRequest,
  InventoryRecord,
  Product,
  SelectOption,
  UpdateInventoryRequest,
  Warehouse,
} from '../../core/models/erp.models';
import { InventoryService } from '../../core/services/inventory.service';
import { ProductService } from '../../core/services/product.service';
import { StockModuleListState } from '../../core/services/stock-module-list-state';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

const reservedQuantityValidator: ValidatorFn = (control): ValidationErrors | null => {
  const quantity = control.get('quantity')?.value;
  const reservedQuantity = control.get('reservedQuantity')?.value;
  if (quantity === null || quantity === '' || reservedQuantity === null || reservedQuantity === '') return null;
  return Number(reservedQuantity) > Number(quantity) ? { reservedExceedsQuantity: true } : null;
};

@Component({
  selector: 'app-inventory-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Inventory"
      subtitle="On-hand and reserved quantities by product and warehouse."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Add inventory record"
      [createDisabled]="loading() || referenceError() !== null || products().length === 0 || warehouses().length === 0 || submitting()"
      [actions]="actions"
      [actionsDisabled]="submitting()"
      [mutationError]="editorOpen() ? null : mutationError()"
      [successMessage]="successMessage()"
      [scopeNotice]="scopeNotice"
      [scopeError]="referenceError()"
      (createRequested)="openCreate()"
      (rowAction)="handleRowAction($event)"
      emptyTitle="No inventory found."
      emptyMessage="Inventory records are not available yet."
    >
      @if (editorOpen()) {
        <app-master-data-form-panel
          master-data-editor
          [form]="form"
          [fields]="fields"
          [title]="editing() ? 'Edit inventory quantities' : 'Create inventory record'"
          [submitLabel]="editing() ? 'Save quantities' : 'Create inventory'"
          [submitting]="submitting()"
          [error]="mutationError()"
          (submitted)="save()"
          (cancelled)="cancelEditor()"
        />
      }
    </app-master-data-list>
  `,
})
export class InventoryPageComponent extends StockModuleListState<InventoryRecord> {
  private readonly inventoryService = inject(InventoryService);
  private readonly productService = inject(ProductService);
  private readonly warehouseService = inject(WarehouseService);
  private recordId: string | null = null;
  readonly products = signal<Product[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly referenceError = signal<string | null>(null);
  readonly editorOpen = signal(false);
  readonly editing = signal(false);

  readonly form = new FormGroup({
    productId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    warehouseId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    quantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    reservedQuantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
  }, { validators: [reservedQuantityValidator] });

  readonly columns: DataTableColumn[] = [
    { key: 'product', label: 'Product / SKU' },
    { key: 'warehouse', label: 'Warehouse' },
    { key: 'quantity', label: 'Quantity' },
    { key: 'reservedQuantity', label: 'Reserved' },
    { key: 'availableQuantity', label: 'Available' },
    { key: 'productCompanyId', label: 'Product Company ID' },
    { key: 'warehouseCompanyId', label: 'Warehouse Company ID' },
    { key: 'updatedAt', label: 'Last Updated' },
  ];

  readonly actions: DataTableRowAction[] = [{ key: 'edit', label: 'Edit' }];
  readonly scopeNotice = 'Inventory lists are global. The backend does not verify that the selected product and warehouse belong to the same company.';

  get fields(): MasterDataFormField[] {
    const fields: MasterDataFormField[] = [
      { key: 'productId', label: 'Product', type: 'select', required: true, options: this.productOptions },
      { key: 'warehouseId', label: 'Warehouse', type: 'select', required: true, options: this.warehouseOptions },
      { key: 'quantity', label: 'Quantity', type: 'number', required: true, min: 0 },
      { key: 'reservedQuantity', label: 'Reserved quantity', type: 'number', required: true, min: 0, hint: 'Cannot exceed quantity.' },
    ];
    return this.editing() ? fields.filter((field) => field.key !== 'productId' && field.key !== 'warehouseId') : fields;
  }

  private get productOptions(): SelectOption[] {
    return this.products().map((product) => ({
      value: product.id,
      label: `${product.name} · ${product.sku} · company ${product.companyId.slice(0, 8)}`,
    }));
  }

  private get warehouseOptions(): SelectOption[] {
    return this.warehouses().map((warehouse) => ({
      value: warehouse.id,
      label: `${warehouse.name} · ${warehouse.code} · company ${warehouse.companyId.slice(0, 8)}`,
    }));
  }

  constructor() {
    super();
    this.load(
      forkJoin({
        inventory: this.inventoryService.list(),
        products: this.productService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load products for inventory references.');
          return of([] as Product[]);
        })),
        warehouses: this.warehouseService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load warehouses for inventory references.');
          return of([] as Warehouse[]);
        })),
      }).pipe(map(({ inventory, products, warehouses }) => {
        this.products.set(products);
        this.warehouses.set(warehouses);
        return inventory;
      })),
      (record) => this.toRow(record),
      'inventory',
    );
  }

  openCreate(): void {
    if (this.submitting() || this.referenceError() || !this.products().length || !this.warehouses().length) return;
    this.editing.set(false);
    this.recordId = null;
    this.mutationError.set(null);
    this.successMessage.set(null);
    this.form.reset();
    this.editorOpen.set(true);
  }

  handleRowAction(event: { action: string; row: Record<string, unknown> }): void {
    if (event.action !== 'edit') return;
    const record = this.records().find((item) => item.id === String(event.row['id']));
    if (!record) return;
    this.editing.set(true);
    this.recordId = record.id;
    this.mutationError.set(null);
    this.successMessage.set(null);
    this.form.reset({
      productId: record.productId,
      warehouseId: record.warehouseId,
      quantity: record.quantity,
      reservedQuantity: record.reservedQuantity,
    });
    this.editorOpen.set(true);
  }

  save(): void {
    if (this.submitting()) return;
    this.mutationError.set(null);
    if (this.form.hasError('reservedExceedsQuantity')) {
      this.form.markAllAsTouched();
      this.mutationError.set('Reserved quantity cannot be greater than quantity.');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (this.editing() && this.recordId) {
      const request: UpdateInventoryRequest = {
        quantity: Number(value.quantity),
        reservedQuantity: Number(value.reservedQuantity),
      };
      this.mutate(this.inventoryService.update(this.recordId, request), 'Inventory updated.', () => this.closeEditor());
      return;
    }
    const request: CreateInventoryRequest = {
      productId: value.productId,
      warehouseId: value.warehouseId,
      quantity: Number(value.quantity),
      reservedQuantity: Number(value.reservedQuantity),
    };
    this.mutate(this.inventoryService.create(request), 'Inventory record created.', () => this.closeEditor());
  }

  cancelEditor(): void {
    this.mutationError.set(null);
    this.closeEditor();
  }

  private closeEditor(): void {
    this.editorOpen.set(false);
    this.form.reset();
    this.recordId = null;
  }

  private toRow(record: InventoryRecord): Record<string, unknown> {
    const product = this.products().find((item) => item.id === record.productId);
    const warehouse = this.warehouses().find((item) => item.id === record.warehouseId);
    return {
      id: record.id,
      product: product ? `${product.name} · ${product.sku}` : record.productId,
      warehouse: warehouse ? `${warehouse.name} · ${warehouse.code}` : record.warehouseId,
      quantity: record.quantity,
      reservedQuantity: record.reservedQuantity,
      availableQuantity: record.availableQuantity,
      productCompanyId: product?.companyId ?? '—',
      warehouseCompanyId: warehouse?.companyId ?? '—',
      updatedAt: record.updatedAt,
    };
  }
}