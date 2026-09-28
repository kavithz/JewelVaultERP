import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { UserRecord } from '../../core/models/erp.models';
import { UserService } from '../../core/services/user.service';
import { DataTableComponent, DataTableColumn } from '../../shared/components/data-table/data-table';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent, DataTableComponent],
  template: `
    <app-page-header title="Users" subtitle="User access and system assignments." />

    <app-data-table
      [columns]="columns"
      [rows]="rows()"
      [loading]="loading()"
      [error]="error()"
      emptyTitle="No users found."
      emptyMessage="User data is not available yet."
    ></app-data-table>
  `,
})
export class UsersPageComponent {
  private readonly userService = inject(UserService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly rows = signal<Record<string, unknown>[]>([]);

  readonly columns: DataTableColumn[] = [
    { key: 'username', label: 'Username' },
    { key: 'email', label: 'Email' },
    { key: 'roleName', label: 'Role' },
    { key: 'status', label: 'Status' },
  ];

  constructor() {
    this.userService.list().subscribe({
      next: (data: UserRecord[]) => {
        this.rows.set(data.map((user) => ({
          username: user.username,
          email: user.email ?? '—',
          roleName: user.roleName ?? 'User',
          status: user.enabled !== false ? 'Active' : 'Inactive',
        })));
        this.loading.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message || 'Unable to load users.');
        this.loading.set(false);
      },
    });
  }
}
