import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DataTableColumn, DataTableComponent, DataTableRowAction } from '../data-table/data-table';
import { PageHeaderComponent } from '../page-header/page-header';

@Component({
  selector: 'app-master-data-list',
  standalone: true,
  imports: [PageHeaderComponent, DataTableComponent],
  template: `
    <app-page-header [title]="title" [subtitle]="subtitle" />
    <div class="list-toolbar">
      @if (scopeNotice) {
        <p class="scope-notice" role="note">{{ scopeNotice }}</p>
      }
      @if (scopeError) {
        <p class="feedback error" role="alert">{{ scopeError }}</p>
      }
      @if (mutationError) {
        <p class="feedback error" role="alert">{{ mutationError }}</p>
      }
      @if (successMessage) {
        <p class="feedback success" role="status">{{ successMessage }}</p>
      }
      @if (createLabel) {
        <button type="button" class="create-button" [disabled]="createDisabled" (click)="createRequested.emit()">
          {{ createLabel }}
        </button>
      }
    </div>
    <ng-content select="[master-data-editor]"></ng-content>
    <app-data-table
      [columns]="columns"
      [rows]="rows"
      [actions]="actions"
      [actionsDisabled]="actionsDisabled"
      [busyRowId]="busyRowId"
      [loading]="loading"
      [error]="error"
      [emptyTitle]="emptyTitle"
      [emptyMessage]="emptyMessage"
      (rowAction)="rowAction.emit($event)"
    ></app-data-table>
  `,
  styles: [`
    .list-toolbar { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 0.75rem; margin: -0.5rem 0 1rem; }
    .scope-notice, .feedback { flex: 1 1 100%; margin: 0; font-size: 0.9rem; }
    .scope-notice { color: var(--secondary-text); }
    .feedback { padding: 0.7rem 0.85rem; border-radius: 0.4rem; }
    .feedback.error { color: var(--ruby); background: var(--ruby-light); border: 1px solid rgb(185 28 28 / 14%); }
    .feedback.success { color: var(--emerald); background: var(--emerald-light); border: 1px solid rgb(4 120 87 / 14%); }
    .create-button { margin-left: auto; padding: 0.62rem 0.9rem; border: 1px solid var(--primary); border-radius: 0.5rem; color: #fff; background: var(--primary); font: inherit; font-size: 0.875rem; font-weight: 600; cursor: pointer; }
    .create-button:hover:not(:disabled) { border-color: var(--primary-dark); background: var(--primary-dark); }
    .create-button:disabled { cursor: not-allowed; opacity: 0.55; }
    .create-button:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    @media (max-width: 600px) { .create-button { width: 100%; } }
  `],
})
export class MasterDataListComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() columns: DataTableColumn[] = [];
  @Input() rows: Record<string, unknown>[] = [];
  @Input() loading = false;
  @Input() error: string | null = null;
  @Input() createLabel = '';
  @Input() createDisabled = false;
  @Input() actions: DataTableRowAction[] = [];
  @Input() actionsDisabled = false;
  @Input() busyRowId: string | null = null;
  @Input() scopeNotice = '';
  @Input() scopeError: string | null = null;
  @Input() mutationError: string | null = null;
  @Input() successMessage: string | null = null;
  @Input() emptyTitle = 'No records found.';
  @Input() emptyMessage = 'No records are available for this view.';
  @Output() createRequested = new EventEmitter<void>();
  @Output() rowAction = new EventEmitter<{ action: string; row: Record<string, unknown> }>();
}