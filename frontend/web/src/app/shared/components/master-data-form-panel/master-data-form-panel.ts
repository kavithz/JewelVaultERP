import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

export interface MasterDataFormOption {
  value: string;
  label: string;
}

export interface MasterDataFormField {
  key: string;
  label: string;
  type: 'text' | 'email' | 'number' | 'date' | 'datetime-local' | 'textarea' | 'select';
  required?: boolean;
  min?: number;
  max?: number;
  maxLength?: number;
  options?: MasterDataFormOption[];
  hint?: string;
}

@Component({
  selector: 'app-master-data-form-panel',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <section class="form-panel" aria-labelledby="master-data-form-title">
      <header class="form-heading">
        <h2 id="master-data-form-title">{{ title }}</h2>
        <button type="button" class="cancel-link" [disabled]="submitting" (click)="cancelled.emit()">Cancel</button>
      </header>
      @if (error) {
        <p class="form-error" role="alert">{{ error }}</p>
      }
      <form [formGroup]="form" (ngSubmit)="submitForm()" novalidate>
        <div class="form-grid">
          @for (field of fields; track field.key) {
            <div class="field" [class.wide-field]="field.type === 'textarea'">
              <label [for]="'master-data-' + field.key">
                {{ field.label }}
                @if (field.required) { <span aria-hidden="true">*</span> }
              </label>
              @if (field.type === 'textarea') {
                <textarea
                  [id]="'master-data-' + field.key"
                  [formControlName]="field.key"
                  [attr.maxlength]="field.maxLength ?? null"
                  [attr.aria-invalid]="hasError(field)"
                  [attr.aria-describedby]="hasError(field) ? 'master-data-' + field.key + '-error' : null"
                  rows="3"
                ></textarea>
              } @else if (field.type === 'select') {
                <select
                  [id]="'master-data-' + field.key"
                  [formControlName]="field.key"
                  [attr.aria-invalid]="hasError(field)"
                  [attr.aria-describedby]="hasError(field) ? 'master-data-' + field.key + '-error' : null"
                >
                  <option value="">Select {{ field.label.toLowerCase() }}</option>
                  @for (option of field.options ?? []; track option.value) {
                    <option [value]="option.value">{{ option.label }}</option>
                  }
                </select>
              } @else {
                <input
                  [id]="'master-data-' + field.key"
                  [type]="field.type"
                  [formControlName]="field.key"
                  [attr.min]="field.min ?? null"
                  [attr.max]="field.max ?? null"
                  [attr.maxlength]="field.maxLength ?? null"
                  [attr.step]="field.type === 'number' ? 'any' : null"
                  [attr.aria-invalid]="hasError(field)"
                  [attr.aria-describedby]="hasError(field) ? 'master-data-' + field.key + '-error' : null"
                />
              }
              @if (field.hint) { <small class="field-hint">{{ field.hint }}</small> }
              @if (validationMessage(field); as message) {
                <small class="field-error" [id]="'master-data-' + field.key + '-error'">{{ message }}</small>
              }
            </div>
          }
        </div>
        @if (showFooter) {
          <footer class="form-actions">
            <button type="button" class="secondary-button" [disabled]="submitting" (click)="cancelled.emit()">Cancel</button>
            <button type="submit" class="primary-button" [disabled]="submitting">
              {{ submitting ? 'Saving...' : submitLabel }}
            </button>
          </footer>
        }
      </form>
    </section>
  `,
  styles: [`
    .form-panel { margin: 0 0 1.25rem; padding: 1rem; border: 1px solid var(--border-color); border-radius: 0.75rem; background: var(--surface); box-shadow: var(--shadow-soft); }
    .form-heading, .form-actions { display: flex; align-items: center; justify-content: space-between; gap: 0.75rem; }
    .form-heading { margin-bottom: 1rem; }
    h2 { margin: 0; color: var(--primary-text); font-size: 1.1rem; font-weight: 600; }
    .cancel-link { padding: 0.4rem; border: 0; background: none; color: var(--accent-strong); font: inherit; cursor: pointer; }
    .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.9rem 1rem; }
    .field { display: grid; align-content: start; gap: 0.35rem; min-width: 0; }
    .wide-field { grid-column: 1 / -1; }
    label { color: var(--primary-text); font-size: 0.82rem; font-weight: 500; }
    input, select, textarea { width: 100%; min-height: 2.65rem; box-sizing: border-box; padding: 0.6rem 0.7rem; border: 1px solid var(--border-color); border-radius: 0.5rem; color: var(--primary-text); background: var(--surface-soft); font: inherit; font-size: 0.9rem; }
    textarea { resize: vertical; }
    input:focus-visible, select:focus-visible, textarea:focus-visible, button:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
    [aria-invalid="true"] { border-color: var(--danger); }
    .field-hint { color: var(--secondary-text); }
    .field-error, .form-error { color: var(--danger); }
    .form-error { margin: 0 0 1rem; }
    .form-actions { justify-content: flex-end; margin-top: 1rem; }
    .secondary-button, .primary-button { min-height: 2.5rem; padding: 0.55rem 0.9rem; border: 1px solid var(--border-color); border-radius: 0.5rem; background: var(--surface); color: var(--primary-text); font: inherit; font-weight: 500; cursor: pointer; }
    .primary-button { border-color: var(--primary); color: #fff; background: var(--primary); font-weight: 600; }
    .primary-button:hover:not(:disabled) { border-color: var(--primary-dark); background: var(--primary-dark); }
    button:disabled { cursor: wait; opacity: 0.6; }
    @media (max-width: 640px) { .form-grid { grid-template-columns: minmax(0, 1fr); } .wide-field { grid-column: auto; } .form-actions > button { flex: 1; } }
  `],
})
export class MasterDataFormPanelComponent {
  @Input({ required: true }) form!: FormGroup;
  @Input() fields: MasterDataFormField[] = [];
  @Input() title = 'Record';
  @Input() submitLabel = 'Save';
  @Input() showFooter = true;
  @Input() submitting = false;
  @Input() error: string | null = null;
  @Output() submitted = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  submitForm(): void {
    this.form.markAllAsTouched();
    if (this.form.valid && !this.submitting) {
      this.submitted.emit();
    }
  }

  hasError(field: MasterDataFormField): boolean {
    const control = this.form.get(field.key);
    return Boolean(control?.touched && control.invalid);
  }

  validationMessage(field: MasterDataFormField): string | null {
    const control = this.form.get(field.key);
    if (!control?.touched || !control.errors) {
      return null;
    }

    if (control.hasError('required')) return `${field.label} is required.`;
    if (control.hasError('email')) return 'Enter a valid email address.';
    if (control.hasError('maxlength')) return `${field.label} is too long.`;
    if (control.hasError('min')) return `${field.label} must be at least ${field.min}.`;
    if (control.hasError('max')) return `${field.label} must be no more than ${field.max}.`;
    if (control.hasError('positive')) return `${field.label} must be greater than zero.`;
    return `${field.label} is invalid.`;
  }
}