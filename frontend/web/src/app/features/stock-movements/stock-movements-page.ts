import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { catchError, forkJoin, map, of } from 'rxjs';
import {
  CreateStockMovementRequest,
  InventoryRecord,
  MovementType,
  Product,
  SelectOption,
  StockMovementRecord,
  Warehouse,
} from '../../core/models/erp.models';
import { InventoryService } from '../../core/services/inventory.service';
import { ProductService } from '../../core/services/product.service';
import { StockModuleListState } from '../../core/services/stock-module-list-state';
import { StockMovementService } from '../../core/services/stock-movement.service';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

const movementTypes: MovementType[] = [
  'PURCHASE',
  'SALE',
  'TRANSFER_IN',
  'TRANSFER_OUT',
  'ADJUSTMENT_IN',
  'ADJUSTMENT_OUT',
  'MANUFACTURING_IN',
  'MANUFACTURING_OUT',
];

@Component({
  selector: 'app-stock-movements-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Stock Movements"
      subtitle="Recorded inventory movements and references."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Record movement"
      [createDisabled]="loading() || referenceError() !== null || inventory().length === 0 || submitting()"
      [mutationError]="editorOpen() ? null : mutationError()"
      [successMessage]="successMessage()"
      [scopeNotice]="scopeNotice"
      [scopeError]="referenceError()"
      (createRequested)="openCreate()"
      emptyTitle="No stock movements found."
      emptyMessage="Movement records are not available yet."
    >
      @if (editorOpen()) {
        <app-master-data-form-panel
          master-data-editor
          [form]="form"
          [fields]="formFields"
          title="Record stock movement"
          submitLabel="Record movement"
          [submitting]="submitting()"
          [error]="mutationError()"
          (submitted)="save()"
          (cancelled)="cancelEditor()"
        />
      }
    </app-master-data-list>
  `,
})
export class StockMovementsPageComponent extends StockModuleListState<StockMovementRecord> {
  private readonly movementService = inject(StockMovementService);
  private readonly inventoryService = inject(InventoryService);
  private readonly productService = inject(ProductService);
  private readonly warehouseService = inject(WarehouseService);
  readonly inventory = signal<InventoryRecord[]>([]);
  readonly products = signal<Product[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly referenceError = signal<string | null>(null);
  readonly editorOpen = signal(false);

  readonly form = new FormGroup({
    inventoryId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    movementType: new FormControl<MovementType | ''>('', { validators: [Validators.required] }),
    quantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.001)] }),
    referenceNumber: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
    notes: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    movementDate: new FormControl('', { nonNullable: true }),
  });

  readonly columns: DataTableColumn[] = [
    { key: 'movementDate', label: 'Movement Date' },
    { key: 'product', label: 'Product / SKU' },
    { key: 'warehouse', label: 'Warehouse' },
    { key: 'productCompanyId', label: 'Product Company ID' },
    { key: 'warehouseCompanyId', label: 'Warehouse Company ID' },
    { key: 'movementType', label: 'Movement Type' },
    { key: 'quantity', label: 'Quantity' },
    { key: 'referenceNumber', label: 'Reference' },
    { key: 'createdAt', label: 'Recorded At' },
  ];

  readonly scopeNotice = 'Movement lists are global. The API accepts an existing inventory ID and applies stock and reserved-quantity rules on the server.';
  private readonly baseFields: MasterDataFormField[] = [
    { key: 'inventoryId', label: 'Inventory record', type: 'select', required: true },
    { key: 'movementType', label: 'Movement type', type: 'select', required: true, options: movementTypes.map((value) => ({ value, label: value.replaceAll('_', ' ') })) },
    { key: 'quantity', label: 'Quantity', type: 'number', required: true, min: 0.001 },
    { key: 'referenceNumber', label: 'Reference number', type: 'text', maxLength: 100 },
    { key: 'notes', label: 'Notes', type: 'textarea', maxLength: 500 },
    { key: 'movementDate', label: 'Movement date and time', type: 'datetime-local', hint: 'Optional; leave blank to use the backend time.' },
  ];

  get formFields(): MasterDataFormField[] {
    return this.baseFields.map((field) => field.key === 'inventoryId'
      ? { ...field, options: this.inventoryOptions }
      : field);
  }

  private get inventoryOptions(): SelectOption[] {
    return this.inventory().map((record) => {
      const product = this.products().find((item) => item.id === record.productId);
      const warehouse = this.warehouses().find((item) => item.id === record.warehouseId);
      const productName = product ? `${product.name} · ${product.sku}` : record.productId;
      const warehouseName = warehouse ? warehouse.name : record.warehouseId;
      return {
        value: record.id,
        label: `${productName} at ${warehouseName} · available ${record.availableQuantity}`,
      };
    });
  }

  constructor() {
    super();
    this.load(
      forkJoin({
        movements: this.movementService.list(),
        inventory: this.inventoryService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load inventory references.');
          return of([] as InventoryRecord[]);
        })),
        products: this.productService.list().pipe(catchError(() => of([] as Product[]))),
        warehouses: this.warehouseService.list().pipe(catchError(() => of([] as Warehouse[]))),
      }).pipe(map(({ movements, inventory, products, warehouses }) => {
        this.inventory.set(inventory);
        this.products.set(products);
        this.warehouses.set(warehouses);
        return movements;
      })),
      (movement) => this.toRow(movement),
      'stock movements',
    );
  }

  openCreate(): void {
    if (this.submitting() || this.referenceError() || !this.inventory().length) return;
    this.mutationError.set(null);
    this.successMessage.set(null);
    this.form.reset();
    this.editorOpen.set(true);
  }

  save(): void {
    if (this.submitting() || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: CreateStockMovementRequest = {
      inventoryId: value.inventoryId,
      movementType: value.movementType as MovementType,
      quantity: Number(value.quantity),
      referenceNumber: value.referenceNumber.trim() || null,
      notes: value.notes.trim() || null,
      ...(value.movementDate ? { movementDate: value.movementDate } : {}),
    };
    this.mutate(this.movementService.create(request), 'Stock movement recorded.', () => this.closeEditor());
  }

  cancelEditor(): void {
    this.mutationError.set(null);
    this.closeEditor();
  }

  private closeEditor(): void {
    this.editorOpen.set(false);
    this.form.reset();
  }

  private toRow(movement: StockMovementRecord): Record<string, unknown> {
    const product = this.products().find((item) => item.id === movement.productId);
    const warehouse = this.warehouses().find((item) => item.id === movement.warehouseId);
    return {
      id: movement.id,
      movementDate: movement.movementDate,
      product: product ? `${product.name} · ${product.sku}` : movement.productId,
      warehouse: warehouse?.name ?? movement.warehouseId,
      productCompanyId: product?.companyId ?? '—',
      warehouseCompanyId: warehouse?.companyId ?? '—',
      movementType: movement.movementType.replaceAll('_', ' '),
      quantity: movement.quantity,
      referenceNumber: movement.referenceNumber,
      createdAt: movement.createdAt,
    };
  }
}