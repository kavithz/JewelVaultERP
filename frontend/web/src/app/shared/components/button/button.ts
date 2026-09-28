import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled"
      [attr.aria-label]="ariaLabel || null"
      class="erp-button"
      [ngClass]="variant"
    >
      <ng-content></ng-content>
    </button>
  `,
  styles: [
    `
      :host { display: inline-block; }
      .erp-button {
        border: 1px solid var(--border-color);
        background: var(--surface-elevated);
        color: var(--primary-text);
        min-height: 2.5rem;
        padding: 0.55rem 0.85rem;
        border-radius: 0.5rem;
        font-weight: 500;
        cursor: pointer;
        transition: var(--transition-fast);
      }
      .erp-button:hover:not(:disabled) { background: var(--surface-soft); border-color: #cbd5e1; }
      .erp-button:disabled { opacity: 0.55; cursor: not-allowed; }
      .erp-button.primary { background: var(--primary); color: var(--white); border-color: var(--primary); font-weight: 600; }
      .erp-button.primary:hover:not(:disabled) { background: var(--primary-dark); border-color: var(--primary-dark); }
      .erp-button.secondary { background: var(--surface); }
      .erp-button.danger { background: var(--ruby); color: white; border-color: var(--ruby); }
      .erp-button.ghost { background: transparent; border-color: transparent; }
    `,
  ],
})
export class ButtonComponent {
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() variant: 'primary' | 'secondary' | 'danger' | 'ghost' = 'primary';
  @Input() disabled = false;
  @Input() ariaLabel: string | null = null;
}
