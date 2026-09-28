import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { take } from 'rxjs';
import { UserRecord } from '../../core/models/erp.models';
import { AuthService } from '../../core/services/auth.service';
import { PreviewDataService, PreviewUser } from '../../core/services/preview-data.service';
import { UserService } from '../../core/services/user.service';
import { DataTableComponent, DataTableColumn } from '../../shared/components/data-table/data-table';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, PageHeaderComponent, DataTableComponent],
  template: `
    @if (previewMode()) {
      <app-page-header title="Users" subtitle="Sample users for Preview Mode." />
      <p class="preview-data-note">Sample users for preview only. Not real JewelVaultERP accounts.</p>
      <app-data-table
        [columns]="previewColumns"
        [rows]="rows()"
        [loading]="loading()"
        [error]="error()"
        emptyTitle="No sample users available."
        emptyMessage="Preview user examples could not be loaded."
      ></app-data-table>
    } @else {
      <app-page-header title="Users" subtitle="User access and system assignments." />

      <app-data-table
        [columns]="columns"
        [rows]="rows()"
        [loading]="loading()"
        [error]="error()"
        emptyTitle="No users found."
        emptyMessage="User data is not available yet."
      ></app-data-table>
    }
  `,
  styles: [`.preview-data-note { margin: 0 0 0.75rem; color: var(--secondary-text); font-size: 0.82rem; }`],
})
export class UsersPageComponent {
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly previewDataService = inject(PreviewDataService);

  readonly previewMode = signal(false);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly rows = signal<Record<string, unknown>[]>([]);

  readonly columns: DataTableColumn[] = [
    { key: 'username', label: 'Username' },
    { key: 'email', label: 'Email' },
    { key: 'roleName', label: 'Role' },
    { key: 'status', label: 'Status' },
  ];

  readonly previewColumns: DataTableColumn[] = [
    { key: 'firstName', label: 'First Name' },
    { key: 'lastName', label: 'Last Name' },
    { key: 'username', label: 'Username' },
    { key: 'email', label: 'Email' },
  ];

  constructor() {
    if (this.authService.isPreviewAuthenticated()) {
      this.previewMode.set(true);
      this.previewDataService.getUsers().pipe(take(1)).subscribe({
        next: (response) => {
          this.rows.set(response.users.map((user: PreviewUser) => ({
            id: user.id,
            firstName: user.firstName,
            lastName: user.lastName,
            username: user.username,
            email: user.email,
          })));
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.error.set(error instanceof Error ? error.message : 'Preview user examples are unavailable.');
          this.loading.set(false);
        },
      });
      return;
    }

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
