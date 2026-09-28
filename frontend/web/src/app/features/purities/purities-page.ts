import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Company, CreatePurityRequest, Purity, UpdatePurityRequest } from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { MasterDataListState } from '../../core/services/master-data-list-state';
import { PurityService } from '../../core/services/purity.service';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

@Component({
  selector: 'app-purities-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Purities"
      subtitle="Purity grades and fineness values."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Add purity"
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
      emptyTitle="No purities found."
      emptyMessage="Purity records are not available yet."
    >
      @if (editorOpen()) {
        <app-master-data-form-panel master-data-editor [form]="form" [fields]="fields"
          [title]="editing() ? 'Edit purity' : 'Create purity'"
          [submitLabel]="editing() ? 'Save changes' : 'Create purity'"
          [submitting]="submitting()" [error]="mutationError()"
          (submitted)="save()" (cancelled)="cancelEditor()" />
      }
    </app-master-data-list>
  `,
})
export class PuritiesPageComponent extends MasterDataListState<Purity> {
  private readonly purityService = inject(PurityService);
  private readonly companyService = inject(CompanyService);
  private recordId: string | null = null;

  readonly form = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    name: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    code: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    fineness: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.001), Validators.max(1000)] }),
    description: new FormControl('', { nonNullable: true }),
  });

  readonly actions: DataTableRowAction[] = [
    { key: 'edit', label: 'Edit' },
    { key: 'toggleStatus', label: '', activeLabel: 'Deactivate', inactiveLabel: 'Activate' },
  ];

  readonly columns: DataTableColumn[] = [
    { key: 'name', label: 'Purity' },
    { key: 'code', label: 'Code' },
    { key: 'fineness', label: 'Fineness' },
    { key: 'description', label: 'Description' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'status', label: 'Status' },
  ];

  readonly scopeNotice = 'The list endpoint returns records across companies. Choose a company explicitly when creating a purity.';

  get fields(): MasterDataFormField[] {
    const fields: MasterDataFormField[] = [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.scopeOptions() },
      { key: 'name', label: 'Purity name', type: 'text', required: true },
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'fineness', label: 'Fineness', type: 'number', required: true, min: 0.001, max: 1000 },
      { key: 'description', label: 'Description', type: 'textarea' },
    ];
    return this.editing() ? fields.filter((field) => field.key !== 'companyId') : fields;
  }

  constructor() {
    super();
    this.load(this.purityService.list(), (purity) => ({
      ...purity,
      status: purity.active ? 'Active' : 'Inactive',
    }), 'purities');
    this.loadOptions(this.companyService.list(), (company: Company) => ({
      value: company.id,
      label: `${company.name} (${company.id.slice(0, 8)})`,
    }), 'No companies are available for purity creation.');
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
    const purity = this.records().find((record) => record.id === String(event.row['id']));
    if (!purity) return;
    if (event.action === 'edit') {
      this.editing.set(true);
      this.recordId = purity.id;
      this.mutationError.set(null);
      this.successMessage.set(null);
      this.form.reset({
        companyId: purity.companyId,
        name: purity.name,
        code: purity.code,
        fineness: purity.fineness,
        description: purity.description ?? '',
      });
      this.editorOpen.set(true);
    } else if (event.action === 'toggleStatus') {
      this.changeStatus(purity.id, !purity.active, this.purityService.setActive(purity.id, !purity.active), 'Purity');
    }
  }

  save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (this.editing() && this.recordId) {
      const request: UpdatePurityRequest = {
        name: value.name,
        code: value.code,
        fineness: Number(value.fineness),
        description: value.description.trim() || null,
      };
      this.saveMutation(this.purityService.update(this.recordId, request), 'Purity updated.', () => this.resetForm());
      return;
    }
    const request: CreatePurityRequest = {
      companyId: value.companyId,
      name: value.name,
      code: value.code,
      fineness: Number(value.fineness),
      description: value.description.trim() || null,
    };
    this.saveMutation(this.purityService.create(request), 'Purity created.', () => this.resetForm());
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