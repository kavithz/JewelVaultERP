import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of, take } from 'rxjs';
import {
  Company,
  CreatePurchaseItemRequest,
  CreatePurchaseRequest,
  Product,
  Supplier,
  Warehouse,
} from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { ProductService } from '../../core/services/product.service';
import { PurchaseService } from '../../core/services/purchase.service';
import { SupplierService } from '../../core/services/supplier.service';
import { WarehouseService } from '../../core/services/warehouse.service';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state';
import { LoadingStateComponent } from '../../shared/components/loading-state/loading-state';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

type PurchaseItemForm = FormGroup<{
  productId: FormControl<string>;
  quantity: FormControl<number | null>;
  unitPrice: FormControl<number | null>;
}>;

@Component({
  selector: 'app-purchase-create-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, PageHeaderComponent, MasterDataFormPanelComponent, LoadingStateComponent, ErrorStateComponent, EmptyStateComponent],
  template: `
    <app-page-header title="Create Purchase" subtitle="Create a draft purchase from verified company, supplier, warehouse, and product records." />
    <a class="back-link" routerLink="/purchases">Back to Purchases</a>

    @if (referenceLoading()) {
      <app-loading-state />
    } @else if (referenceError()) {
      <app-error-state [message]="referenceError()!" />
    } @else if (!companies().length || !suppliers().length || !warehouses().length || !products().length) {
      <app-empty-state title="Purchase options unavailable." message="A company, supplier, warehouse, and product are required before a purchase can be created." />
    } @else {
      <section class="purchase-editor" aria-label="Create purchase form">
        <app-master-data-form-panel
          [form]="headerForm"
          [fields]="fields"
          title="Purchase details"
          submitLabel="Create draft purchase"
          [showFooter]="false"
          [submitting]="submitting()"
          [error]="submitError()"
          (submitted)="save()"
        />
        <section class="items-section" aria-labelledby="purchase-items-heading">
          <div class="items-heading">
            <div>
              <h2 id="purchase-items-heading">Purchase items</h2>
              <p>Subtotal and total are calculated by the backend. Receiving the purchase later creates stock movements.</p>
            </div>
            <button type="button" class="add-item" [disabled]="submitting()" (click)="addItem()">Add item</button>
          </div>
          @if (itemError()) { <p class="field-error" role="alert">{{ itemError() }}</p> }
          <div class="item-list">
            @for (item of items.controls; track item; let index = $index) {
              <div class="item-row" [formGroup]="item">
                <div class="item-field">
                  <label [for]="'purchase-product-' + index">Product</label>
                  <select [id]="'purchase-product-' + index" formControlName="productId" required>
                    <option value="">Select product</option>
                    @for (product of products(); track product.id) {
                      <option [value]="product.id">{{ product.name }} · {{ product.sku }} · company {{ product.companyId.slice(0, 8) }}</option>
                    }
                  </select>
                  @if (item.controls.productId.touched && item.controls.productId.invalid) { <small class="field-error">Product is required.</small> }
                </div>
                <div class="item-field">
                  <label [for]="'purchase-quantity-' + index">Quantity</label>
                  <input [id]="'purchase-quantity-' + index" type="number" min="0.001" step="any" formControlName="quantity" required />
                  @if (item.controls.quantity.touched && item.controls.quantity.errors?.['required']) { <small class="field-error">Quantity is required.</small> }
                  @if (item.controls.quantity.touched && item.controls.quantity.errors?.['min']) { <small class="field-error">Quantity must be at least 0.001.</small> }
                </div>
                <div class="item-field">
                  <label [for]="'purchase-unit-price-' + index">Unit price</label>
                  <input [id]="'purchase-unit-price-' + index" type="number" min="0" step="any" formControlName="unitPrice" required />
                  @if (item.controls.unitPrice.touched && item.controls.unitPrice.errors?.['required']) { <small class="field-error">Unit price is required.</small> }
                  @if (item.controls.unitPrice.touched && item.controls.unitPrice.errors?.['min']) { <small class="field-error">Unit price cannot be negative.</small> }
                </div>
                <button type="button" class="remove-item" [disabled]="submitting() || items.length <= 1" [attr.aria-label]="'Remove purchase item ' + (index + 1)" (click)="removeItem(index)">Remove</button>
              </div>
            }
          </div>
          <footer class="items-actions">
            <a class="cancel-button" routerLink="/purchases">Cancel</a>
            <button type="button" class="primary-button" [disabled]="submitting()" (click)="save()">{{ submitting() ? 'Saving...' : 'Create draft purchase' }}</button>
          </footer>
        </section>
      </section>
    }
  `,
  styles: [`
    :host { display: block; min-width: 0; }
    .back-link { display: inline-block; margin: 0 0 1rem; color: var(--accent-strong); }
    .purchase-editor { min-width: 0; }
    .items-section { margin-top: -1.25rem; padding: 0 1rem 1rem; border: 1px solid var(--border-color); border-top: 0; border-radius: 0 0 0.5rem 0.5rem; background: #fff; }
    .items-heading { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 0.75rem; }
    h2 { margin: 0; color: var(--primary-text); font-size: 1rem; }
    .items-heading p { margin: 0.3rem 0 0; color: var(--secondary-text); font-size: 0.85rem; }
    .add-item, .remove-item, .cancel-button, .primary-button { min-height: 2.4rem; padding: 0.45rem 0.7rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--accent-strong); font: inherit; text-decoration: none; cursor: pointer; }
    .item-list { display: grid; gap: 0.65rem; }
    .item-row { display: grid; grid-template-columns: minmax(12rem, 2fr) minmax(7rem, 1fr) minmax(7rem, 1fr) auto; align-items: start; gap: 0.75rem; }
    .item-field { display: grid; gap: 0.3rem; min-width: 0; }
    label { color: var(--primary-text); font-size: 0.82rem; font-weight: 500; }
    select, input { width: 100%; min-height: 2.5rem; box-sizing: border-box; padding: 0.55rem 0.65rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--primary-text); font: inherit; }
    button:focus-visible, a:focus-visible, input:focus-visible, select:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    button:disabled { opacity: 0.55; cursor: not-allowed; }
    .field-error { color: var(--danger); }
    .items-actions { display: flex; justify-content: flex-end; gap: 0.65rem; margin-top: 1rem; }
    .primary-button { border-color: var(--accent-strong); background: var(--accent-strong); color: #fff; font-weight: 600; }
    @media (max-width: 760px) { .item-row { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); } .remove-item { justify-self: start; } }
    @media (max-width: 460px) { .items-heading { align-items: stretch; flex-direction: column; } .item-row { grid-template-columns: minmax(0, 1fr); } .items-actions > * { flex: 1; text-align: center; } }
  `],
})
export class PurchaseCreatePageComponent {
  private readonly companyService = inject(CompanyService);
  private readonly supplierService = inject(SupplierService);
  private readonly warehouseService = inject(WarehouseService);
  private readonly productService = inject(ProductService);
  private readonly purchaseService = inject(PurchaseService);
  private readonly router = inject(Router);

  readonly companies = signal<Company[]>([]);
  readonly suppliers = signal<Supplier[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly products = signal<Product[]>([]);
  readonly referenceLoading = signal(true);
  readonly referenceError = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);
  readonly itemError = signal<string | null>(null);

  readonly headerForm = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    supplierId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    warehouseId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    purchaseNumber: new FormControl('', { nonNullable: true, validators: [notBlankValidator, Validators.maxLength(100)] }),
    purchaseDate: new FormControl('', { nonNullable: true }),
    taxAmount: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    discountAmount: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
  });

  readonly items = new FormArray<PurchaseItemForm>([]);

  get fields(): MasterDataFormField[] {
    return [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.companies().map((company) => ({ value: company.id, label: `${company.name} (${company.id.slice(0, 8)})` })) },
      { key: 'supplierId', label: 'Supplier', type: 'select', required: true, options: this.suppliers().map((supplier) => ({ value: supplier.id, label: `${supplier.name} · ${supplier.code} · company ${supplier.companyId.slice(0, 8)}` })) },
      { key: 'warehouseId', label: 'Warehouse', type: 'select', required: true, options: this.warehouses().map((warehouse) => ({ value: warehouse.id, label: `${warehouse.name} · ${warehouse.code} · company ${warehouse.companyId.slice(0, 8)}` })) },
      { key: 'purchaseNumber', label: 'Purchase number', type: 'text', required: true, maxLength: 100 },
      { key: 'purchaseDate', label: 'Purchase date and time', type: 'datetime-local' },
      { key: 'taxAmount', label: 'Tax amount', type: 'number', required: true, min: 0 },
      { key: 'discountAmount', label: 'Discount amount', type: 'number', required: true, min: 0 },
    ];
  }

  constructor() {
    this.items.push(this.createItemForm());
    forkJoin({
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
    }).pipe(take(1)).subscribe(({ companies, suppliers, warehouses, products }) => {
      this.companies.set(companies);
      this.suppliers.set(suppliers);
      this.warehouses.set(warehouses);
      this.products.set(products);
      this.referenceLoading.set(false);
    });
  }

  addItem(): void {
    if (!this.submitting()) this.items.push(this.createItemForm());
  }

  removeItem(index: number): void {
    if (!this.submitting() && this.items.length > 1) this.items.removeAt(index);
  }

  save(): void {
    if (this.submitting()) return;
    this.submitError.set(null);
    this.itemError.set(null);
    this.headerForm.markAllAsTouched();
    this.items.controls.forEach((item) => item.markAllAsTouched());
    if (!this.items.length) {
      this.itemError.set('At least one purchase item is required.');
      return;
    }
    if (this.headerForm.invalid || this.items.invalid) return;

    const value = this.headerForm.getRawValue();
    const request: CreatePurchaseRequest = {
      companyId: value.companyId,
      supplierId: value.supplierId,
      warehouseId: value.warehouseId,
      purchaseNumber: value.purchaseNumber.trim(),
      ...(value.purchaseDate ? { purchaseDate: value.purchaseDate } : {}),
      taxAmount: Number(value.taxAmount),
      discountAmount: Number(value.discountAmount),
      items: this.items.getRawValue().map((item): CreatePurchaseItemRequest => ({
        productId: item.productId,
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
      })),
    };

    this.submitting.set(true);
    this.purchaseService.create(request).pipe(take(1)).subscribe({
      next: (purchase) => {
        void this.router.navigate(['/purchases', purchase.id]);
      },
      error: (error: unknown) => {
        this.submitError.set(error instanceof Error ? error.message : 'Unable to create purchase.');
        this.submitting.set(false);
      },
      complete: () => this.submitting.set(false),
    });
  }

  private createItemForm(): PurchaseItemForm {
    return new FormGroup({
      productId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      quantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.001)] }),
      unitPrice: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    });
  }
}