import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { take } from 'rxjs';
import { Company, CreateProductRequest, Product, UpdateProductRequest } from '../../core/models/erp.models';
import { notBlankValidator, positiveNumberValidator } from '../../core/forms/validators';
import { AuthService } from '../../core/services/auth.service';
import { CompanyService } from '../../core/services/company.service';
import { MasterDataListState } from '../../core/services/master-data-list-state';
import { ProductService } from '../../core/services/product.service';
import { PreviewDataService, PreviewProduct } from '../../core/services/preview-data.service';
import { DataTableColumn, DataTableComponent, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-products-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent, DataTableComponent, PageHeaderComponent],
  template: `
    @if (previewMode()) {
      <app-page-header title="Products" subtitle="Sample products for Preview Mode." />
      <p class="preview-data-note">Sample products only. Not jewellery items.</p>
      <app-data-table
        [columns]="previewColumns"
        [rows]="rows()"
        [loading]="loading()"
        [error]="error()"
        emptyTitle="No sample products available."
        emptyMessage="Preview product examples could not be loaded."
      />
    } @else {
      <app-master-data-list
        title="Products"
        subtitle="Product catalogue and jewellery specifications."
        [columns]="columns"
        [rows]="rows()"
        [loading]="loading()"
        [error]="error()"
        createLabel="Add product"
        [createDisabled]="scopeLoading() || scopeError() !== null || scopeOptions().length === 0 || submitting() || statusBusyId() !== null"
        [actions]="actions"
        [actionsDisabled]="submitting() || statusBusyId() !== null"
        [busyRowId]="statusBusyId()"
        [scopeNotice]="scopeNotice"
        [scopeError]="scopeError()"
        [mutationError]="editorOpen() ? null : mutationError()"
        [successMessage]="successMessage()"
        (createRequested)="openCreate()"
        (rowAction)="handleRowAction($event)"
        emptyTitle="No products found."
        emptyMessage="Product records are not available yet."
      >
        @if (editorOpen()) {
          <app-master-data-form-panel
            master-data-editor
            [form]="form"
            [fields]="fields"
            [title]="editing() ? 'Edit product' : 'Create product'"
            [submitLabel]="editing() ? 'Save changes' : 'Create product'"
            [submitting]="submitting()"
            [error]="mutationError()"
            (submitted)="save()"
            (cancelled)="cancelEditor()"
          />
        }
      </app-master-data-list>
    }
  `,
  styles: [`.preview-data-note { margin: 0 0 0.75rem; color: var(--secondary-text); font-size: 0.82rem; }`],
})
export class ProductsPageComponent extends MasterDataListState<Product> {
  private readonly productService = inject(ProductService);
  private readonly companyService = inject(CompanyService);
  private readonly authService = inject(AuthService);
  private readonly previewDataService = inject(PreviewDataService);
  private recordId: string | null = null;
  readonly previewMode = signal(false);

  readonly form = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    sku: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    name: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    jewelleryType: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    metalType: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    purity: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    grossWeight: new FormControl<number | null>(null, { validators: [Validators.required, positiveNumberValidator] }),
    netWeight: new FormControl<number | null>(null, { validators: [Validators.required, positiveNumberValidator] }),
    makingCharge: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
  });

  readonly actions: DataTableRowAction[] = [
    { key: 'edit', label: 'Edit' },
    { key: 'toggleStatus', label: '', activeLabel: 'Deactivate', inactiveLabel: 'Activate' },
  ];

  readonly columns: DataTableColumn[] = [
    { key: 'sku', label: 'SKU' },
    { key: 'name', label: 'Product' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'jewelleryType', label: 'Jewellery Type' },
    { key: 'metalType', label: 'Metal' },
    { key: 'purity', label: 'Purity' },
    { key: 'grossWeight', label: 'Gross Weight' },
    { key: 'netWeight', label: 'Net Weight' },
    { key: 'makingCharge', label: 'Making Charge' },
    { key: 'status', label: 'Status' },
  ];

  readonly previewColumns: DataTableColumn[] = [
    { key: 'title', label: 'Product' },
    { key: 'brand', label: 'Brand' },
    { key: 'sku', label: 'SKU' },
    { key: 'category', label: 'Category' },
    { key: 'price', label: 'Price' },
    { key: 'stock', label: 'Stock' },
    { key: 'availabilityStatus', label: 'Availability' },
  ];

  readonly scopeNotice = 'The list endpoint returns records across companies. Choose a company explicitly when creating a product.';

  get fields(): MasterDataFormField[] {
    const fields: MasterDataFormField[] = [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.scopeOptions() },
      { key: 'sku', label: 'SKU', type: 'text', required: true },
      { key: 'name', label: 'Product name', type: 'text', required: true },
      { key: 'jewelleryType', label: 'Jewellery type', type: 'text', required: true },
      { key: 'metalType', label: 'Metal', type: 'text', required: true },
      { key: 'purity', label: 'Purity', type: 'text', required: true },
      { key: 'grossWeight', label: 'Gross weight', type: 'number', required: true, min: 0, hint: 'Must be greater than zero.' },
      { key: 'netWeight', label: 'Net weight', type: 'number', required: true, min: 0, hint: 'Must be greater than zero.' },
      { key: 'makingCharge', label: 'Making charge', type: 'number', required: true, min: 0 },
    ];
    return this.editing() ? fields.filter((field) => field.key !== 'companyId') : fields;
  }

  constructor() {
    super();
    if (this.authService.isPreviewAuthenticated()) {
      this.previewMode.set(true);
      this.previewDataService.getProducts().pipe(take(1)).subscribe({
        next: (response) => {
          this.rows.set(response.products.map((product: PreviewProduct) => ({
            id: product.id,
            title: product.title,
            brand: product.brand ?? '—',
            sku: product.sku,
            category: product.category,
            price: product.price,
            stock: product.stock,
            availabilityStatus: product.availabilityStatus,
          })));
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.error.set(error instanceof Error ? error.message : 'Preview product examples are unavailable.');
          this.loading.set(false);
        },
      });
      return;
    }
    this.load(this.productService.list(), (product) => ({
      ...product,
      status: product.active ? 'Active' : 'Inactive',
    }), 'products');
    this.loadOptions(this.companyService.list(), (company: Company) => ({
      value: company.id,
      label: `${company.name} (${company.id.slice(0, 8)})`,
    }), 'No companies are available for product creation.');
  }

  openCreate(): void {
    if (this.scopeLoading() || this.scopeError() || !this.scopeOptions().length) return;
    this.editing.set(false);
    this.recordId = null;
    this.mutationError.set(null);
    this.successMessage.set(null);
    this.form.reset();
    this.editorOpen.set(true);
  }

  handleRowAction(event: { action: string; row: Record<string, unknown> }): void {
    const product = this.records().find((record) => record.id === String(event.row['id']));
    if (!product) return;
    if (event.action === 'edit') {
      this.editing.set(true);
      this.recordId = product.id;
      this.mutationError.set(null);
      this.successMessage.set(null);
      this.form.reset({
        companyId: product.companyId,
        sku: product.sku,
        name: product.name,
        jewelleryType: product.jewelleryType ?? '',
        metalType: product.metalType ?? '',
        purity: product.purity ?? '',
        grossWeight: product.grossWeight,
        netWeight: product.netWeight,
        makingCharge: product.makingCharge,
      });
      this.editorOpen.set(true);
    } else if (event.action === 'toggleStatus') {
      this.changeStatus(product.id, !product.active, this.productService.setActive(product.id, !product.active), 'Product');
    }
  }

  save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (this.editing() && this.recordId) {
      const request: UpdateProductRequest = {
        sku: value.sku,
        name: value.name,
        jewelleryType: value.jewelleryType,
        metalType: value.metalType,
        purity: value.purity,
        grossWeight: Number(value.grossWeight),
        netWeight: Number(value.netWeight),
        makingCharge: Number(value.makingCharge),
      };
      this.saveMutation(this.productService.update(this.recordId, request), 'Product updated.', () => this.resetForm());
      return;
    }

    const request: CreateProductRequest = {
      companyId: value.companyId,
      sku: value.sku,
      name: value.name,
      jewelleryType: value.jewelleryType,
      metalType: value.metalType,
      purity: value.purity,
      grossWeight: Number(value.grossWeight),
      netWeight: Number(value.netWeight),
      makingCharge: Number(value.makingCharge),
    };
    this.saveMutation(this.productService.create(request), 'Product created.', () => this.resetForm());
  }

  cancelEditor(): void {
    this.editorOpen.set(false);
    this.mutationError.set(null);
    this.resetForm();
  }

  private resetForm(): void {
    this.form.reset();
    this.recordId = null;
  }
}