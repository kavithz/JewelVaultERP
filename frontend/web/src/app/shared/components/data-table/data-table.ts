import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EmptyStateComponent } from '../empty-state/empty-state';
import { ErrorStateComponent } from '../error-state/error-state';
import { LoadingStateComponent } from '../loading-state/loading-state';

export interface DataTableColumn {
  key: string;
  label: string;
}

export interface DataTableRowAction {
  key: string;
  label: string;
  activeLabel?: string;
  inactiveLabel?: string;
  visibleWhen?: (row: Record<string, unknown>) => boolean;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, LoadingStateComponent, ErrorStateComponent, EmptyStateComponent],
  template: `
    @if (loading) {
      <app-loading-state></app-loading-state>
    } @else if (error) {
      <app-error-state [message]="error"></app-error-state>
    } @else if (!rows.length) {
      <app-empty-state [title]="emptyTitle" [message]="emptyMessage"></app-empty-state>
    } @else {
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              @for (column of columns; track column.key) {
                <th>{{ column.label }}</th>
              }
              @if (actions.length) {
                <th scope="col">Actions</th>
              }
            </tr>
          </thead>
          <tbody>
            @for (row of rows; track row) {
              <tr>
                @for (column of columns; track column.key) {
                  <td>{{ row[column.key] ?? '—' }}</td>
                }
                @if (actions.length) {
                  <td class="row-actions">
                    @for (action of actions; track action.key) {
                      @if (!action.visibleWhen || action.visibleWhen(row)) {
                      <button
                        type="button"
                        [disabled]="actionsDisabled || busyRowId === row['id']"
                        [attr.aria-label]="busyRowId === row['id'] ? 'Updating ' + rowDescription(row) : actionLabel(action, row) + ' ' + rowDescription(row)"
                        (click)="rowAction.emit({ action: action.key, row })"
                      >
                        {{ busyRowId === row['id'] ? 'Updating...' : actionLabel(action, row) }}
                      </button>
                      }
                    }
                  </td>
                }
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
  styles: [
    `
      :host { display: block; }
      .table-wrap { overflow-x: auto; }
      table {
        width: 100%;
        border-collapse: collapse;
        min-width: 640px;
        background: var(--surface);
      }
      th, td {
        padding: 0.72rem 0.85rem;
        border-bottom: 1px solid var(--border-color);
        text-align: left;
        color: var(--primary-text);
      }
      th {
        font-size: 0.72rem;
        letter-spacing: 0.04em;
        text-transform: uppercase;
        color: var(--secondary-text);
        background: var(--surface-soft);
        font-weight: 600;
        white-space: nowrap;
      }
      td { font-size: 0.875rem; font-weight: 400; line-height: 1.45; }
      tr:hover td { background: #f8faff; }
      .row-actions { white-space: nowrap; }
      .row-actions button {
        margin-right: 0.4rem;
        padding: 0.38rem 0.55rem;
        border: 1px solid var(--border-color);
        border-radius: 0.45rem;
        background: var(--surface);
        color: var(--primary);
        font: inherit;
        font-size: 0.8rem;
        font-weight: 500;
        cursor: pointer;
      }
      .row-actions button:hover:not(:disabled) { border-color: var(--gold); background: var(--gold-light); color: var(--primary-dark); }
      .row-actions button:disabled { cursor: wait; opacity: 0.6; }
      .row-actions button:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    `,
  ],
})
export class DataTableComponent {
  @Input() columns: DataTableColumn[] = [];
  @Input() rows: Record<string, unknown>[] = [];
  @Input() actions: DataTableRowAction[] = [];
  @Input() actionsDisabled = false;
  @Input() busyRowId: string | null = null;
  @Input() loading = false;
  @Input() error: string | null = null;
  @Input() emptyTitle = 'No records found.';
  @Input() emptyMessage = 'No records are available for this view.';
  @Output() rowAction = new EventEmitter<{ action: string; row: Record<string, unknown> }>();

  actionLabel(action: DataTableRowAction, row: Record<string, unknown>): string {
    if (action.activeLabel && action.inactiveLabel) {
      return row['active'] === true ? action.activeLabel : action.inactiveLabel;
    }
    return action.label;
  }

  rowDescription(row: Record<string, unknown>): string {
    return String(row['name'] ?? row['sku'] ?? row['code'] ?? 'record');
  }
}
