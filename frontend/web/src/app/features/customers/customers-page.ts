import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Company, CreateCustomerRequest, Customer, UpdateCustomerRequest } from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { CustomerService } from '../../core/services/customer.service';
import { MasterDataListState } from '../../core/services/master-data-list-state';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

@Component({
  selector: 'app-customers-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Customers"
      subtitle="Customer directory and contact information."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Add customer"
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
      emptyTitle="No customers found."
      emptyMessage="Customer records are not available yet."
    >
      @if (editorOpen()) {
        <app-master-data-form-panel master-data-editor [form]="form" [fields]="fields"
          [title]="editing() ? 'Edit customer' : 'Create customer'"
          [submitLabel]="editing() ? 'Save changes' : 'Create customer'"
          [submitting]="submitting()" [error]="mutationError()"
          (submitted)="save()" (cancelled)="cancelEditor()" />
      }
    </app-master-data-list>
  `,
})
export class CustomersPageComponent extends MasterDataListState<Customer> {
  private readonly customerService = inject(CustomerService);
  private readonly companyService = inject(CompanyService);
  private recordId: string | null = null;

  readonly form = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    name: new FormControl('', { nonNullable: true, validators: [notBlankValidator, Validators.maxLength(150)] }),
    code: new FormControl('', { nonNullable: true, validators: [notBlankValidator, Validators.maxLength(50)] }),
    phone: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.email, Validators.maxLength(255)] }),
    address: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(255)] }),
    taxNumber: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
  });

  readonly actions: DataTableRowAction[] = [
    { key: 'edit', label: 'Edit' },
    { key: 'toggleStatus', label: '', activeLabel: 'Deactivate', inactiveLabel: 'Activate' },
  ];

  readonly columns: DataTableColumn[] = [
    { key: 'name', label: 'Customer' },
    { key: 'code', label: 'Code' },
    { key: 'phone', label: 'Phone' },
    { key: 'email', label: 'Email' },
    { key: 'taxNumber', label: 'Tax Number' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'status', label: 'Status' },
  ];

  readonly scopeNotice = 'The list endpoint returns records across companies. Choose a company explicitly when creating a customer.';

  get fields(): MasterDataFormField[] {
    const fields: MasterDataFormField[] = [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.scopeOptions() },
      { key: 'name', label: 'Customer name', type: 'text', required: true, maxLength: 150 },
      { key: 'code', label: 'Code', type: 'text', required: true, maxLength: 50 },
      { key: 'phone', label: 'Phone', type: 'text', maxLength: 50 },
      { key: 'email', label: 'Email', type: 'email', maxLength: 255 },
      { key: 'address', label: 'Address', type: 'textarea', maxLength: 255 },
      { key: 'taxNumber', label: 'Tax number', type: 'text', maxLength: 100 },
    ];
    return this.editing() ? fields.filter((field) => field.key !== 'companyId') : fields;
  }

  constructor() {
    super();
    this.load(this.customerService.list(), (customer) => ({
      ...customer,
      status: customer.active ? 'Active' : 'Inactive',
    }), 'customers');
    this.loadOptions(this.companyService.list(), (company: Company) => ({
      value: company.id,
      label: `${company.name} (${company.id.slice(0, 8)})`,
    }), 'No companies are available for customer creation.');
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
    const customer = this.records().find((record) => record.id === String(event.row['id']));
    if (!customer) return;
    if (event.action === 'edit') {
      this.editing.set(true);
      this.recordId = customer.id;
      this.mutationError.set(null);
      this.successMessage.set(null);
      this.form.reset({
        companyId: customer.companyId,
        name: customer.name,
        code: customer.code,
        phone: customer.phone ?? '',
        email: customer.email ?? '',
        address: customer.address ?? '',
        taxNumber: customer.taxNumber ?? '',
      });
      this.editorOpen.set(true);
    } else if (event.action === 'toggleStatus') {
      this.changeStatus(customer.id, !customer.active, this.customerService.setActive(customer.id, !customer.active), 'Customer');
    }
  }

  save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const optional = {
      phone: value.phone.trim() || null,
      email: value.email.trim() || null,
      address: value.address.trim() || null,
      taxNumber: value.taxNumber.trim() || null,
    };
    if (this.editing() && this.recordId) {
      const request: UpdateCustomerRequest = { name: value.name, code: value.code, ...optional };
      this.saveMutation(this.customerService.update(this.recordId, request), 'Customer updated.', () => this.resetForm());
      return;
    }
    const request: CreateCustomerRequest = { companyId: value.companyId, name: value.name, code: value.code, ...optional };
    this.saveMutation(this.customerService.create(request), 'Customer created.', () => this.resetForm());
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