import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Company } from '../../core/models/erp.models';
import { CompanyService } from '../../core/services/company.service';
import { DataTableComponent, DataTableColumn } from '../../shared/components/data-table/data-table';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-companies-page',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent, DataTableComponent],
  template: `
    <app-page-header title="Companies" subtitle="Company registry and operating context." />

    <app-data-table
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      emptyTitle="No companies found."
      emptyMessage="Company records are not available yet."
    ></app-data-table>
  `,
})
export class CompaniesPageComponent {
  private readonly companyService = inject(CompanyService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly rows = signal<Record<string, unknown>[]>([]);

  readonly columns: DataTableColumn[] = [
    { key: 'name', label: 'Company' },
    { key: 'legalName', label: 'Legal Name' },
    { key: 'countryCode', label: 'Country' },
    { key: 'currencyCode', label: 'Currency' },
  ];

  constructor() {
    this.companyService.list().subscribe({
      next: (data: Company[]) => {
        this.rows.set(data.map((company) => ({
          name: company.name,
          legalName: company.legalName ?? '—',
          countryCode: company.countryCode ?? '—',
          currencyCode: company.currencyCode ?? '—',
        })));
        this.loading.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message || 'Unable to load companies.');
        this.loading.set(false);
      },
    });
  }
}
