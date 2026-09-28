import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Company, CreateGemstoneRequest, Gemstone, UpdateGemstoneRequest } from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { CompanyService } from '../../core/services/company.service';
import { GemstoneService } from '../../core/services/gemstone.service';
import { MasterDataListState } from '../../core/services/master-data-list-state';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

@Component({
  selector: 'app-gemstones-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Gemstones"
      subtitle="Gemstone catalogue and classification details."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Add gemstone"
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
      emptyTitle="No gemstones found."
      emptyMessage="Gemstone records are not available yet."
    >
      @if (editorOpen()) {
        <app-master-data-form-panel master-data-editor [form]="form" [fields]="fields"
          [title]="editing() ? 'Edit gemstone' : 'Create gemstone'"
          [submitLabel]="editing() ? 'Save changes' : 'Create gemstone'"
          [submitting]="submitting()" [error]="mutationError()"
          (submitted)="save()" (cancelled)="cancelEditor()" />
      }
    </app-master-data-list>
  `,
})
export class GemstonesPageComponent extends MasterDataListState<Gemstone> {
  private readonly gemstoneService = inject(GemstoneService);
  private readonly companyService = inject(CompanyService);
  private recordId: string | null = null;

  readonly form = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    name: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    code: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    category: new FormControl('', { nonNullable: true }),
    color: new FormControl('', { nonNullable: true }),
    description: new FormControl('', { nonNullable: true }),
  });

  readonly actions: DataTableRowAction[] = [
    { key: 'edit', label: 'Edit' },
    { key: 'toggleStatus', label: '', activeLabel: 'Deactivate', inactiveLabel: 'Activate' },
  ];

  readonly columns: DataTableColumn[] = [
    { key: 'name', label: 'Gemstone' },
    { key: 'code', label: 'Code' },
    { key: 'category', label: 'Category' },
    { key: 'color', label: 'Color' },
    { key: 'description', label: 'Description' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'status', label: 'Status' },
  ];

  readonly scopeNotice = 'The list endpoint returns records across companies. Choose a company explicitly when creating a gemstone.';

  get fields(): MasterDataFormField[] {
    const fields: MasterDataFormField[] = [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.scopeOptions() },
      { key: 'name', label: 'Gemstone name', type: 'text', required: true },
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'category', label: 'Category', type: 'text' },
      { key: 'color', label: 'Color', type: 'text' },
      { key: 'description', label: 'Description', type: 'textarea' },
    ];
    return this.editing() ? fields.filter((field) => field.key !== 'companyId') : fields;
  }

  constructor() {
    super();
    this.load(this.gemstoneService.list(), (gemstone) => ({
      ...gemstone,
      status: gemstone.active ? 'Active' : 'Inactive',
    }), 'gemstones');
    this.loadOptions(this.companyService.list(), (company: Company) => ({
      value: company.id,
      label: `${company.name} (${company.id.slice(0, 8)})`,
    }), 'No companies are available for gemstone creation.');
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
    const gemstone = this.records().find((record) => record.id === String(event.row['id']));
    if (!gemstone) return;
    if (event.action === 'edit') {
      this.editing.set(true);
      this.recordId = gemstone.id;
      this.mutationError.set(null);
      this.successMessage.set(null);
      this.form.reset({
        companyId: gemstone.companyId,
        name: gemstone.name,
        code: gemstone.code,
        category: gemstone.category ?? '',
        color: gemstone.color ?? '',
        description: gemstone.description ?? '',
      });
      this.editorOpen.set(true);
    } else if (event.action === 'toggleStatus') {
      this.changeStatus(gemstone.id, !gemstone.active, this.gemstoneService.setActive(gemstone.id, !gemstone.active), 'Gemstone');
    }
  }

  save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (this.editing() && this.recordId) {
      const request: UpdateGemstoneRequest = {
        name: value.name,
        code: value.code,
        category: value.category.trim() || null,
        color: value.color.trim() || null,
        description: value.description.trim() || null,
      };
      this.saveMutation(this.gemstoneService.update(this.recordId, request), 'Gemstone updated.', () => this.resetForm());
      return;
    }
    const request: CreateGemstoneRequest = {
      companyId: value.companyId,
      name: value.name,
      code: value.code,
      category: value.category.trim() || null,
      color: value.color.trim() || null,
      description: value.description.trim() || null,
    };
    this.saveMutation(this.gemstoneService.create(request), 'Gemstone created.', () => this.resetForm());
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