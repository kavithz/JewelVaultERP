import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { take } from 'rxjs';
import { EmployeeRecord } from '../../core/models/erp.models';
import { EmployeeService } from '../../core/services/employee.service';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state';
import { LoadingStateComponent } from '../../shared/components/loading-state/loading-state';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-employee-details-page',
  standalone: true,
  imports: [RouterLink, PageHeaderComponent, ErrorStateComponent, LoadingStateComponent],
  template: `
    <app-page-header [title]="employee() ? employee()!.firstName + ' ' + (employee()!.lastName ?? '') : 'Employee details'" subtitle="Employee record and employment information." />
    <a class="back-link" routerLink="/employees">Back to Employees</a>

    @if (loading()) {
      <app-loading-state />
    } @else if (error()) {
      <app-error-state [message]="error()!" />
    } @else if (employee(); as record) {
      <section class="details-panel" aria-labelledby="employee-details-heading">
        <h2 id="employee-details-heading">Employee details</h2>
        <dl class="details-grid">
          <div><dt>Employee number</dt><dd>{{ record.employeeNumber }}</dd></div>
          <div><dt>Status</dt><dd>{{ record.employmentStatus.replaceAll('_', ' ') }}</dd></div>
          <div><dt>Company ID</dt><dd>{{ record.companyId }}</dd></div>
          <div><dt>Branch ID</dt><dd>{{ record.branchId ?? '—' }}</dd></div>
          <div><dt>First name</dt><dd>{{ record.firstName }}</dd></div>
          <div><dt>Last name</dt><dd>{{ record.lastName ?? '—' }}</dd></div>
          <div><dt>Email</dt><dd>{{ record.email ?? '—' }}</dd></div>
          <div><dt>Phone</dt><dd>{{ record.phone ?? '—' }}</dd></div>
          <div><dt>Address</dt><dd>{{ record.address ?? '—' }}</dd></div>
          <div><dt>Department</dt><dd>{{ record.department ?? '—' }}</dd></div>
          <div><dt>Designation</dt><dd>{{ record.designation ?? '—' }}</dd></div>
          <div><dt>Joining date</dt><dd>{{ record.joiningDate }}</dd></div>
          <div><dt>Exit date</dt><dd>{{ record.exitDate ?? '—' }}</dd></div>
          <div><dt>Created</dt><dd>{{ record.createdAt }}</dd></div>
          <div><dt>Last updated</dt><dd>{{ record.updatedAt }}</dd></div>
        </dl>
      </section>
    }
  `,
  styles: [`
    :host { display: block; min-width: 0; }
    .back-link { display: inline-block; margin: 0 0 1rem; color: var(--accent-strong); }
    .details-panel { min-width: 0; padding: 1rem; border: 1px solid var(--border-color); border-radius: 0.45rem; background: #fff; }
    h2 { margin: 0 0 1rem; color: var(--primary-text); font-size: 1.05rem; }
    .details-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.85rem 1.5rem; margin: 0; }
    .details-grid > div { min-width: 0; }
    dt { color: var(--secondary-text); font-size: 0.8rem; }
    dd { overflow-wrap: anywhere; margin: 0.2rem 0 0; color: var(--primary-text); }
    @media (max-width: 600px) { .details-grid { grid-template-columns: minmax(0, 1fr); } }
  `],
})
export class EmployeeDetailsPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly employeeService = inject(EmployeeService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly employee = signal<EmployeeRecord | null>(null);

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Employee ID is missing from the route.');
      this.loading.set(false);
      return;
    }

    this.employeeService.get(id).pipe(take(1)).subscribe({
      next: (employee) => {
        this.employee.set(employee);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(error instanceof Error ? error.message : 'Unable to load employee details.');
        this.loading.set(false);
      },
    });
  }
}