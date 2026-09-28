import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Branch, CreateWarehouseRequest, UpdateWarehouseRequest, Warehouse } from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { BranchService } from '../../core/services/branch.service';
import { MasterDataListState } from '../../core/services/master-data-list-state';
import { WarehouseService } from '../../core/services/warehouse.service';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

@Component({
  selector: 'app-warehouses-page',
  standalone: true,
  imports: [MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Warehouses"
      subtitle="Warehouse locations and branch assignments."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Add warehouse"
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
      emptyTitle="No warehouses found."
      emptyMessage="Warehouse records are not available yet."
    >
      @if (editorOpen()) {
        <app-master-data-form-panel master-data-editor [form]="form" [fields]="fields"
          [title]="editing() ? 'Edit warehouse' : 'Create warehouse'"
          [submitLabel]="editing() ? 'Save changes' : 'Create warehouse'"
          [submitting]="submitting()" [error]="mutationError()"
          (submitted)="save()" (cancelled)="cancelEditor()" />
      }
    </app-master-data-list>
  `,
})
export class WarehousesPageComponent extends MasterDataListState<Warehouse> {
  private readonly warehouseService = inject(WarehouseService);
  private readonly branchService = inject(BranchService);
  private recordId: string | null = null;

  readonly form = new FormGroup({
    branchId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    name: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    code: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    address: new FormControl('', { nonNullable: true }),
    description: new FormControl('', { nonNullable: true }),
  });

  readonly actions: DataTableRowAction[] = [
    { key: 'edit', label: 'Edit' },
    { key: 'toggleStatus', label: '', activeLabel: 'Deactivate', inactiveLabel: 'Activate' },
  ];

  readonly columns: DataTableColumn[] = [
    { key: 'name', label: 'Warehouse' },
    { key: 'code', label: 'Code' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'branchId', label: 'Branch ID' },
    { key: 'address', label: 'Address' },
    { key: 'status', label: 'Status' },
  ];

  readonly scopeNotice = 'The list endpoint returns warehouses across companies. Choose an existing branch explicitly when creating a warehouse.';

  get fields(): MasterDataFormField[] {
    const fields: MasterDataFormField[] = [
      { key: 'branchId', label: 'Branch', type: 'select', required: true, options: this.scopeOptions() },
      { key: 'name', label: 'Warehouse name', type: 'text', required: true },
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'address', label: 'Address', type: 'textarea' },
      { key: 'description', label: 'Description', type: 'textarea' },
    ];
    return this.editing() ? fields.filter((field) => field.key !== 'branchId') : fields;
  }

  constructor() {
    super();
    this.load(this.warehouseService.list(), (warehouse) => ({
      ...warehouse,
      status: warehouse.active ? 'Active' : 'Inactive',
    }), 'warehouses');
    this.loadOptions(this.branchService.list(), (branch: Branch) => ({
      value: branch.id,
      label: `${branch.name}${branch.code ? ` (${branch.code})` : ''} — company ${branch.companyId?.slice(0, 8) ?? 'unknown'}`,
    }), 'No branches are available for warehouse creation.');
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
    const warehouse = this.records().find((record) => record.id === String(event.row['id']));
    if (!warehouse) return;
    if (event.action === 'edit') {
      this.editing.set(true);
      this.recordId = warehouse.id;
      this.mutationError.set(null);
      this.successMessage.set(null);
      this.form.reset({
        branchId: warehouse.branchId,
        name: warehouse.name,
        code: warehouse.code,
        address: warehouse.address ?? '',
        description: warehouse.description ?? '',
      });
      this.editorOpen.set(true);
    } else if (event.action === 'toggleStatus') {
      this.changeStatus(warehouse.id, !warehouse.active, this.warehouseService.setActive(warehouse.id, !warehouse.active), 'Warehouse');
    }
  }

  save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (this.editing() && this.recordId) {
      const request: UpdateWarehouseRequest = {
        name: value.name,
        code: value.code,
        address: value.address.trim() || null,
        description: value.description.trim() || null,
      };
      this.saveMutation(this.warehouseService.update(this.recordId, request), 'Warehouse updated.', () => this.resetForm());
      return;
    }
    const request: CreateWarehouseRequest = {
      branchId: value.branchId,
      name: value.name,
      code: value.code,
      address: value.address.trim() || null,
      description: value.description.trim() || null,
    };
    this.saveMutation(this.warehouseService.create(request), 'Warehouse created.', () => this.resetForm());
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