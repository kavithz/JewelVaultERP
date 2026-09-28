import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-state',
  standalone: true,
  template: `
    <div class="error-state" role="alert">
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
    </div>
  `,
  styles: [
    `
      .error-state {
        display: grid;
        gap: 0.5rem;
        padding: 1.1rem 1.2rem;
        border: 1px solid rgb(185 28 28 / 16%);
        border-left: 3px solid var(--ruby);
        border-radius: 0.6rem;
        background: var(--ruby-light);
        color: var(--primary-text);
      }
      .error-state h3 { margin: 0; color: var(--ruby); font-size: 0.95rem; font-weight: 600; }
      .error-state p { margin: 0; color: var(--primary-text); font-size: 0.875rem; overflow-wrap: anywhere; }
    `,
  ],
})
export class ErrorStateComponent {
  @Input() title = 'Unable to load this data.';
  @Input() message = 'Unable to load this data. Please try again.';
}
