import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import { Branch, Company, CreateEmployeeRequest, EmployeeRecord, EmploymentStatus } from '../../core/models/erp.models';
import { notBlankValidator } from '../../core/forms/validators';
import { BranchService } from '../../core/services/branch.service';
import { CompanyService } from '../../core/services/company.service';
import { EmployeeService } from '../../core/services/employee.service';
import { StockModuleListState } from '../../core/services/stock-module-list-state';
import { DataTableColumn, DataTableRowAction } from '../../shared/components/data-table/data-table';
import { MasterDataFormField, MasterDataFormPanelComponent } from '../../shared/components/master-data-form-panel/master-data-form-panel';
import { MasterDataListComponent } from '../../shared/components/master-data-list/master-data-list';

const employmentStatuses: EmploymentStatus[] = ['ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED', 'RESIGNED'];

type EditorMode = 'create' | 'status' | 'exitDate';

@Component({
  selector: 'app-employees-page',
  standalone: true,
  imports: [CommonModule, MasterDataListComponent, MasterDataFormPanelComponent],
  template: `
    <app-master-data-list
      title="Employees"
      subtitle="Employee records and employment status."
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      createLabel="Add employee"
      [createDisabled]="submitting() || referenceError() !== null || companies().length === 0"
      [actions]="actions"
      [actionsDisabled]="submitting()"
      [busyRowId]="busyRowId()"
      [scopeNotice]="scopeNotice"
      [scopeError]="referenceError()"
      [mutationError]="editorOpen() ? null : mutationError()"
      [successMessage]="successMessage()"
      (createRequested)="openCreate()"
      (rowAction)="handleRowAction($event)"
      emptyTitle="No employees found."
      emptyMessage="Employee records are not available yet."
    >
      <div master-data-editor class="filter-toolbar" aria-label="Employee API filters">
        <label for="employee-company-filter">Company</label>
        <select id="employee-company-filter" [value]="activeFilter()" (change)="filterByCompany($any($event.target).value)">
          <option value="">All companies</option>
          @for (company of companies(); track company.id) {
            <option [value]="'company:' + company.id">{{ company.name }}</option>
          }
        </select>
        <label for="employee-branch-filter">Branch</label>
        <select id="employee-branch-filter" [value]="activeFilter()" (change)="filterByBranch($any($event.target).value)">
          <option value="">All branches</option>
          @for (branch of branches(); track branch.id) {
            <option [value]="'branch:' + branch.id">{{ branch.name }} · {{ branch.companyId?.slice(0, 8) ?? 'company unavailable' }}</option>
          }
        </select>
        <label for="employee-status-filter">Status</label>
        <select id="employee-status-filter" [value]="activeFilter()" (change)="filterByStatus($any($event.target).value)">
          <option value="">All statuses</option>
          @for (status of employmentStatuses; track status) {
            <option [value]="'status:' + status">{{ status.replaceAll('_', ' ') }}</option>
          }
        </select>
        <button type="button" [disabled]="activeFilter() === ''" (click)="clearFilter()">Clear filter</button>
      </div>
      @if (editorOpen()) {
        <app-master-data-form-panel
          master-data-editor
          [form]="activeForm"
          [fields]="formFields"
          [title]="editorTitle"
          [submitLabel]="submitLabel"
          [submitting]="submitting()"
          [error]="mutationError()"
          (submitted)="submitEditor()"
          (cancelled)="cancelEditor()"
        />
      }
    </app-master-data-list>
  `,
  styles: [`
    .filter-toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 0.5rem 0.65rem; margin-bottom: 0.75rem; }
    .filter-toolbar label { color: var(--secondary-text); font-size: 0.82rem; font-weight: 500; }
    .filter-toolbar select, .filter-toolbar button { min-height: 2.35rem; max-width: 100%; padding: 0.4rem 0.55rem; border: 1px solid var(--border-color); border-radius: 0.4rem; background: #fff; color: var(--primary-text); font: inherit; }
    .filter-toolbar button { color: var(--accent-strong); cursor: pointer; }
    .filter-toolbar button:disabled { opacity: 0.55; cursor: not-allowed; }
    .filter-toolbar :focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    @media (max-width: 720px) { .filter-toolbar label:not(:first-child) { margin-left: 0; } .filter-toolbar select { flex: 1 1 9rem; } }
  `],
})
export class EmployeesPageComponent extends StockModuleListState<EmployeeRecord> {
  private readonly employeeService = inject(EmployeeService);
  private readonly companyService = inject(CompanyService);
  private readonly branchService = inject(BranchService);
  private readonly router = inject(Router);
  private selectedEmployeeId: string | null = null;

  readonly companies = signal<Company[]>([]);
  readonly branches = signal<Branch[]>([]);
  readonly referenceError = signal<string | null>(null);
  readonly editorOpen = signal(false);
  readonly editorMode = signal<EditorMode>('create');
  readonly activeFilter = signal('');
  readonly employmentStatuses = employmentStatuses;

  readonly createForm = new FormGroup({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    branchId: new FormControl('', { nonNullable: true }),
    employeeNumber: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    firstName: new FormControl('', { nonNullable: true, validators: [notBlankValidator] }),
    lastName: new FormControl('', { nonNullable: true }),
    email: new FormControl('', { nonNullable: true }),
    phone: new FormControl('', { nonNullable: true }),
    address: new FormControl('', { nonNullable: true }),
    department: new FormControl('', { nonNullable: true }),
    designation: new FormControl('', { nonNullable: true }),
    joiningDate: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly statusForm = new FormGroup({
    employmentStatus: new FormControl<EmploymentStatus | ''>('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly exitDateForm = new FormGroup({
    exitDate: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly columns: DataTableColumn[] = [
    { key: 'employeeNumber', label: 'Employee Number' },
    { key: 'employeeName', label: 'Employee' },
    { key: 'companyId', label: 'Company ID' },
    { key: 'branchId', label: 'Branch ID' },
    { key: 'department', label: 'Department' },
    { key: 'designation', label: 'Designation' },
    { key: 'employmentStatus', label: 'Status' },
    { key: 'joiningDate', label: 'Joining Date' },
    { key: 'exitDate', label: 'Exit Date' },
  ];

  readonly actions: DataTableRowAction[] = [
    { key: 'view', label: 'View' },
    { key: 'changeStatus', label: 'Change status' },
    { key: 'setExitDate', label: 'Set exit date' },
  ];

  readonly scopeNotice = 'The default employee list is global. These filters call the backend company, branch, or status endpoints; they are not an authorization boundary.';

  get activeForm(): FormGroup {
    if (this.editorMode() === 'status') return this.statusForm;
    if (this.editorMode() === 'exitDate') return this.exitDateForm;
    return this.createForm;
  }

  get formFields(): MasterDataFormField[] {
    if (this.editorMode() === 'status') {
      return [{ key: 'employmentStatus', label: 'Employment status', type: 'select', required: true, options: employmentStatuses.map((status) => ({ value: status, label: status.replaceAll('_', ' ') })) }];
    }
    if (this.editorMode() === 'exitDate') {
      return [{ key: 'exitDate', label: 'Exit date', type: 'date', required: true }];
    }
    return [
      { key: 'companyId', label: 'Company', type: 'select', required: true, options: this.companies().map((company) => ({ value: company.id, label: `${company.name} (${company.id.slice(0, 8)})` })) },
      { key: 'branchId', label: 'Branch', type: 'select', options: [{ value: '', label: 'No branch' }, ...this.branches().map((branch) => ({ value: branch.id, label: `${branch.name} · ${branch.companyId?.slice(0, 8) ?? 'company unavailable'}` }))] },
      { key: 'employeeNumber', label: 'Employee number', type: 'text', required: true },
      { key: 'firstName', label: 'First name', type: 'text', required: true },
      { key: 'lastName', label: 'Last name', type: 'text' },
      { key: 'email', label: 'Email', type: 'email' },
      { key: 'phone', label: 'Phone', type: 'text' },
      { key: 'address', label: 'Address', type: 'textarea' },
      { key: 'department', label: 'Department', type: 'text' },
      { key: 'designation', label: 'Designation', type: 'text' },
      { key: 'joiningDate', label: 'Joining date', type: 'date', required: true },
    ];
  }

  get editorTitle(): string {
    if (this.editorMode() === 'status') return 'Change employee status';
    if (this.editorMode() === 'exitDate') return 'Set employee exit date';
    return 'Create employee';
  }

  get submitLabel(): string {
    if (this.editorMode() === 'status') return 'Update status';
    if (this.editorMode() === 'exitDate') return 'Set exit date';
    return 'Create employee';
  }

  constructor() {
    super();
    this.load(
      forkJoin({
        employees: this.employeeService.list(),
        companies: this.companyService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load companies.');
          return of([] as Company[]);
        })),
        branches: this.branchService.list().pipe(catchError((error: unknown) => {
          this.referenceError.set(error instanceof Error ? error.message : 'Unable to load branches.');
          return of([] as Branch[]);
        })),
      }).pipe(map(({ employees, companies, branches }) => {
        this.companies.set(companies);
        this.branches.set(branches);
        return employees;
      })),
      (employee) => this.toRow(employee),
      'employees',
    );
  }

  filterByCompany(value: string): void {
    if (!value) {
      this.clearFilter();
      return;
    }
    const companyId = value.startsWith('company:') ? value.slice('company:'.length) : value;
    this.activeFilter.set(`company:${companyId}`);
    this.load(this.employeeService.byCompany(companyId), (employee) => this.toRow(employee), 'employees');
  }

  filterByBranch(value: string): void {
    if (!value) {
      this.clearFilter();
      return;
    }
    const branchId = value.startsWith('branch:') ? value.slice('branch:'.length) : value;
    this.activeFilter.set(`branch:${branchId}`);
    this.load(this.employeeService.byBranch(branchId), (employee) => this.toRow(employee), 'employees');
  }

  filterByStatus(value: string): void {
    if (!value) {
      this.clearFilter();
      return;
    }
    const status = (value.startsWith('status:') ? value.slice('status:'.length) : value) as EmploymentStatus;
    if (!employmentStatuses.includes(status)) return;
    this.activeFilter.set(`status:${status}`);
    this.load(this.employeeService.byStatus(status), (employee) => this.toRow(employee), 'employees');
  }

  clearFilter(): void {
    this.activeFilter.set('');
    this.load(this.employeeService.list(), (employee) => this.toRow(employee), 'employees');
  }

  openCreate(): void {
    if (this.submitting() || this.referenceError() || !this.companies().length) return;
    this.editorMode.set('create');
    this.selectedEmployeeId = null;
    this.mutationError.set(null);
    this.successMessage.set(null);
    this.createForm.reset();
    this.editorOpen.set(true);
  }

  handleRowAction(event: { action: string; row: Record<string, unknown> }): void {
    const employee = this.records().find((record) => record.id === String(event.row['id']));
    if (!employee) return;
    if (event.action === 'view') {
      void this.router.navigate(['/employees', employee.id]);
      return;
    }
    this.selectedEmployeeId = employee.id;
    this.mutationError.set(null);
    this.successMessage.set(null);
    if (event.action === 'changeStatus') {
      this.editorMode.set('status');
      this.statusForm.reset({ employmentStatus: employee.employmentStatus });
    } else if (event.action === 'setExitDate') {
      this.editorMode.set('exitDate');
      this.exitDateForm.reset({ exitDate: employee.exitDate ?? '' });
    } else {
      return;
    }
    this.editorOpen.set(true);
  }

  submitEditor(): void {
    if (this.submitting() || this.activeForm.invalid) {
      this.activeForm.markAllAsTouched();
      return;
    }
    if (this.editorMode() === 'create') {
      const value = this.createForm.getRawValue();
      const request: CreateEmployeeRequest = {
        companyId: value.companyId,
        branchId: value.branchId || null,
        employeeNumber: value.employeeNumber.trim(),
        firstName: value.firstName.trim(),
        lastName: value.lastName.trim() || null,
        email: value.email.trim() || null,
        phone: value.phone.trim() || null,
        address: value.address.trim() || null,
        department: value.department.trim() || null,
        designation: value.designation.trim() || null,
        joiningDate: value.joiningDate,
      };
      this.mutate(this.employeeService.create(request), 'Employee created.', () => this.closeEditor());
      return;
    }

    if (!this.selectedEmployeeId) return;
    if (this.editorMode() === 'status') {
      const status = this.statusForm.getRawValue().employmentStatus as EmploymentStatus;
      const current = this.records().find((record) => record.id === this.selectedEmployeeId);
      if (current?.employmentStatus === status) {
        this.mutationError.set('The employee already has this status.');
        return;
      }
      if ((status === 'TERMINATED' || status === 'RESIGNED') && !window.confirm(`Set this employee's status to ${status.replaceAll('_', ' ')}?`)) return;
      this.mutate(this.employeeService.updateStatus(this.selectedEmployeeId, status), `Employee status set to ${status.replaceAll('_', ' ')}.`, () => this.closeEditor(), this.selectedEmployeeId);
      return;
    }

    const exitDate = this.exitDateForm.getRawValue().exitDate;
    if (!window.confirm(`Set the employee exit date to ${exitDate}?`)) return;
    this.mutate(this.employeeService.setExitDate(this.selectedEmployeeId, exitDate), 'Employee exit date updated.', () => this.closeEditor(), this.selectedEmployeeId);
  }

  cancelEditor(): void {
    this.mutationError.set(null);
    this.closeEditor();
  }

  private closeEditor(): void {
    this.editorOpen.set(false);
    this.selectedEmployeeId = null;
    this.createForm.reset();
    this.statusForm.reset();
    this.exitDateForm.reset();
  }

  private toRow(employee: EmployeeRecord): Record<string, unknown> {
    const company = this.companies().find((item) => item.id === employee.companyId);
    const branch = this.branches().find((item) => item.id === employee.branchId);
    const employeeName = `${employee.firstName} ${employee.lastName ?? ''}`.trim();
    return {
      id: employee.id,
      name: employeeName,
      employeeNumber: employee.employeeNumber,
      employeeName,
      companyId: company ? `${company.name} · ${employee.companyId}` : employee.companyId,
      branchId: branch ? `${branch.name} · ${employee.branchId}` : employee.branchId ?? '—',
      department: employee.department,
      designation: employee.designation,
      employmentStatus: employee.employmentStatus.replaceAll('_', ' '),
      joiningDate: employee.joiningDate,
      exitDate: employee.exitDate,
      employmentStatusRaw: employee.employmentStatus,
    };
  }
}