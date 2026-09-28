import { signal } from '@angular/core';
import { Observable, take } from 'rxjs';

export abstract class StockModuleListState<T extends { id: string }> {
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly records = signal<T[]>([]);
  readonly rows = signal<Record<string, unknown>[]>([]);
  readonly submitting = signal(false);
  readonly mutationError = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly busyRowId = signal<string | null>(null);

  private source: Observable<T[]> | null = null;
  private mapRecord: ((record: T) => Record<string, unknown>) | null = null;
  private resourceLabel = 'records';

  protected load(
    source: Observable<T[]>,
    mapRecord: (record: T) => Record<string, unknown>,
    resourceLabel: string,
  ): void {
    this.source = source;
    this.mapRecord = mapRecord;
    this.resourceLabel = resourceLabel;
    this.loading.set(true);
    this.error.set(null);
    source.pipe(take(1)).subscribe({
      next: (records) => {
        this.records.set(records);
        this.rows.set(records.map(mapRecord));
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(error instanceof Error ? error.message : `Unable to load ${resourceLabel}.`);
        this.loading.set(false);
      },
    });
  }

  protected mutate(
    source: Observable<unknown>,
    successMessage: string,
    onSuccess: () => void = () => undefined,
    busyRowId: string | null = null,
  ): void {
    if (this.submitting()) return;
    this.submitting.set(true);
    this.busyRowId.set(busyRowId);
    this.mutationError.set(null);
    this.successMessage.set(null);
    source.pipe(take(1)).subscribe({
      next: () => {
        this.submitting.set(false);
        this.busyRowId.set(null);
        this.successMessage.set(successMessage);
        onSuccess();
        this.refresh();
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        this.busyRowId.set(null);
        this.mutationError.set(error instanceof Error ? error.message : 'The request could not be completed.');
      },
    });
  }

  protected refresh(): void {
    if (this.source && this.mapRecord) {
      this.load(this.source, this.mapRecord, this.resourceLabel);
    }
  }
}