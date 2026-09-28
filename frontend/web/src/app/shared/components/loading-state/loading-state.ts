import { Component } from '@angular/core';

@Component({
  selector: 'app-loading-state',
  standalone: true,
  template: `
    <div class="loading-state" role="status" aria-live="polite">
      <div class="spinner"></div>
      <span>Loading data...</span>
    </div>
  `,
  styles: [
    `
      .loading-state {
        display: inline-flex;
        align-items: center;
        gap: 0.75rem;
        padding: 1rem 0.75rem;
        color: var(--secondary-text);
        font-size: 0.875rem;
      }
      .spinner {
        width: 1rem;
        height: 1rem;
        border-radius: 50%;
        border: 2px solid var(--gold-light);
        border-top-color: var(--primary);
        animation: spin 0.9s linear infinite;
      }
      @keyframes spin { to { transform: rotate(360deg); } }
    `,
  ],
})
export class LoadingStateComponent {}
