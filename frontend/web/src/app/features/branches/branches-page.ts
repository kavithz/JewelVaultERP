import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { BranchRecord } from '../../core/models/erp.models';
import { BranchService } from '../../core/services/branch.service';
import { DataTableComponent, DataTableColumn } from '../../shared/components/data-table/data-table';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-branches-page',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent, DataTableComponent],
  template: `
    <app-page-header title="Branches" subtitle="Branch network and operating locations." />

    <app-data-table
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      emptyTitle="No branches found."
      emptyMessage="Branch records are not available yet."
    ></app-data-table>
  `,
})
export class BranchesPageComponent {
  private readonly branchService = inject(BranchService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly rows = signal<Record<string, unknown>[]>([]);

  readonly columns: DataTableColumn[] = [
    { key: 'name', label: 'Branch' },
    { key: 'city', label: 'City' },
    { key: 'state', label: 'State' },
    { key: 'country', label: 'Country' },
  ];

  constructor() {
    this.branchService.list().subscribe({
      next: (data: BranchRecord[]) => {
        this.rows.set(data.map((branch) => ({
          name: branch.name,
          city: branch.city ?? '—',
          state: branch.state ?? '—',
          country: branch.country ?? '—',
        })));
        this.loading.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message || 'Unable to load branches.');
        this.loading.set(false);
      },
    });
  }
}
