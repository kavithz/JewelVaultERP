import { signal } from '@angular/core';
import { Observable, take } from 'rxjs';
import { SelectOption } from '../models/erp.models';

export abstract class MasterDataListState<T extends { id: string; active: boolean }> {
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly rows = signal<Record<string, unknown>[]>([]);
  readonly records = signal<T[]>([]);
  readonly editorOpen = signal(false);
  readonly editing = signal(false);
  readonly submitting = signal(false);
  readonly mutationError = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly statusBusyId = signal<string | null>(null);
  readonly scopeOptions = signal<SelectOption[]>([]);
  readonly scopeLoading = signal(false);
  readonly scopeError = signal<string | null>(null);

  private listSource: Observable<T[]> | null = null;
  private rowMapper: ((record: T) => Record<string, unknown>) | null = null;
  private resourceName = 'records';

  protected load(
    source: Observable<T[]>,
    toRow: (record: T) => Record<string, unknown>,
    resourceName: string,
  ): void {
    this.listSource = source;
    this.rowMapper = toRow;
    this.resourceName = resourceName;
    this.loading.set(true);
    this.error.set(null);
    source.pipe(take(1)).subscribe({
      next: (records) => {
        this.records.set(records);
        this.rows.set(records.map(toRow));
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(error instanceof Error ? error.message : `Unable to load ${resourceName}.`);
        this.loading.set(false);
      },
    });
  }

  protected loadOptions<TOption>(
    source: Observable<TOption[]>,
    toOption: (record: TOption) => SelectOption,
    emptyMessage?: string,
  ): void {
    this.scopeLoading.set(true);
    this.scopeError.set(null);
    source.pipe(take(1)).subscribe({
      next: (options) => {
        this.scopeOptions.set(options.map(toOption));
        if (!options.length && emptyMessage) {
          this.scopeError.set(emptyMessage);
        }
        this.scopeLoading.set(false);
      },
      error: (error: unknown) => {
        this.scopeError.set(error instanceof Error ? error.message : 'Unable to load available scope options.');
        this.scopeLoading.set(false);
      },
    });
  }

  protected refresh(): void {
    if (this.listSource && this.rowMapper) {
      this.load(this.listSource, this.rowMapper, this.resourceName);
    }
  }

  protected saveMutation(source: Observable<unknown>, message: string, onSuccess: () => void): void {
    if (this.submitting()) {
      return;
    }

    this.submitting.set(true);
    this.mutationError.set(null);
    this.successMessage.set(null);
    source.pipe(take(1)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.editorOpen.set(false);
        this.successMessage.set(message);
        onSuccess();
        this.refresh();
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        this.mutationError.set(error instanceof Error ? error.message : 'The request could not be completed.');
      },
    });
  }

  protected changeStatus(
    id: string,
    active: boolean,
    source: Observable<unknown>,
    resourceName: string,
  ): void {
    if (this.statusBusyId() !== null || this.submitting()) {
      return;
    }

    this.statusBusyId.set(id);
    this.mutationError.set(null);
    this.successMessage.set(null);
    source.pipe(take(1)).subscribe({
      next: () => {
        this.statusBusyId.set(null);
        this.successMessage.set(`${resourceName} ${active ? 'activated' : 'deactivated'}.`);
        this.refresh();
      },
      error: (error: unknown) => {
        this.statusBusyId.set(null);
        this.mutationError.set(error instanceof Error ? error.message : 'The status change could not be completed.');
      },
    });
  }
}