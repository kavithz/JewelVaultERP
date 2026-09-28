import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-state" role="status" aria-live="polite">
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
    </div>
  `,
  styles: [
    `
      .empty-state {
        display: grid;
        gap: 0.5rem;
        padding: 2.25rem 1rem;
        border: 1px dashed var(--border-color);
        border-radius: 0.65rem;
        background: var(--surface-soft);
        text-align: center;
        color: var(--secondary-text);
      }
      .empty-state h3 { margin: 0; color: var(--primary-text); font-size: 1rem; font-weight: 600; }
      .empty-state p { margin: 0; font-size: 0.875rem; }
    `,
  ],
})
export class EmptyStateComponent {
  @Input() title = 'No records found.';
  @Input() message = 'No records are available for this view.';
}
