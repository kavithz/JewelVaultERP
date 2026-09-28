import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Company, CreateJewelleryTypeRequest, JewelleryType, UpdateJewelleryTypeRequest } from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { JewelleryTypeService } from '../../core/services/jewellery-type.service';
import { MasterDataListState } from '../../core/services/master-data-list-state';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

@Component({
  selector: 'app-jewellery-types-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Jewellery Types"
      subtitle="Jewellery classifications used in the catalogue."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Add jewellery type"
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
      emptyTitle="No jewellery types found."
      emptyMessage="Jewellery type records are not available yet."
    >
      @if (editorOpen()) {
        <app-master-data-form-panel master-data-editor [form]="form" [fields]="fields"
          [title]="editing() ? 'Edit jewellery type' : 'Create jewellery type'"
          [submitLabel]="editing() ? 'Save changes' : 'Create jewellery type'"
          [submitting]="submitting()" [error]="mutationError()"
          (submitted)="save()" (cancelled)="cancelEditor()" />
      }
    </app-master-data-list>
  `,
})
export class JewelleryTypesPageComponent extends MasterDataListState<JewelleryType> {
  private readonly jewelleryTypeService = inject(JewelleryTypeService);
  private readonly companyService = inject(CompanyService);
  private recordId: string | null = null;

  readonly form = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    name: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    code: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    description: new FormControl('', { nonNullable: true }),
  });

  readonly actions: DataTableRowAction[] = [
    { key: 'edit', label: 'Edit' },
    { key: 'toggleStatus', label: '', activeLabel: 'Deactivate', inactiveLabel: 'Activate' },
  ];

  readonly columns: DataTableColumn[] = [
    { key: 'name', label: 'Jewellery Type' },
    { key: 'code', label: 'Code' },
    { key: 'description', label: 'Description' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'status', label: 'Status' },
  ];

  readonly scopeNotice = 'The list endpoint returns records across companies. Choose a company explicitly when creating a jewellery type.';

  get fields(): MasterDataFormField[] {
    const fields: MasterDataFormField[] = [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.scopeOptions() },
      { key: 'name', label: 'Jewellery type name', type: 'text', required: true },
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'description', label: 'Description', type: 'textarea' },
    ];
    return this.editing() ? fields.filter((field) => field.key !== 'companyId') : fields;
  }

  constructor() {
    super();
    this.load(this.jewelleryTypeService.list(), (type) => ({
      ...type,
      status: type.active ? 'Active' : 'Inactive',
    }), 'jewellery types');
    this.loadOptions(this.companyService.list(), (company: Company) => ({
      value: company.id,
      label: `${company.name} (${company.id.slice(0, 8)})`,
    }), 'No companies are available for jewellery type creation.');
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
    const type = this.records().find((record) => record.id === String(event.row['id']));
    if (!type) return;
    if (event.action === 'edit') {
      this.editing.set(true);
      this.recordId = type.id;
      this.mutationError.set(null);
      this.successMessage.set(null);
      this.form.reset({
        companyId: type.companyId,
        name: type.name,
        code: type.code,
        description: type.description ?? '',
      });
      this.editorOpen.set(true);
    } else if (event.action === 'toggleStatus') {
      this.changeStatus(type.id, !type.active, this.jewelleryTypeService.setActive(type.id, !type.active), 'Jewellery type');
    }
  }

  save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (this.editing() && this.recordId) {
      const request: UpdateJewelleryTypeRequest = {
        name: value.name,
        code: value.code,
        description: value.description.trim() || null,
      };
      this.saveMutation(this.jewelleryTypeService.update(this.recordId, request), 'Jewellery type updated.', () => this.resetForm());
      return;
    }
    const request: CreateJewelleryTypeRequest = {
      companyId: value.companyId,
      name: value.name,
      code: value.code,
      description: value.description.trim() || null,
    };
    this.saveMutation(this.jewelleryTypeService.create(request), 'Jewellery type created.', () => this.resetForm());
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