import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-card',
  standalone: true,
  template: `
    <section class="erp-card">
      @if (title) {
        <div class="card-header">
          <h3>{{ title }}</h3>
        </div>
      }
      <ng-content></ng-content>
    </section>
  `,
  styles: [
    `
      :host { display: block; }
      .erp-card {
        background: var(--surface);
        border: 1px solid var(--border-color);
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-soft);
        padding: 1.1rem 1.15rem;
      }
      .card-header {
        border-bottom: 1px solid var(--border-color);
        margin-bottom: 0.85rem;
        padding-bottom: 0.7rem;
      }
      .card-header h3 {
        margin: 0;
        font-size: 1rem;
        font-weight: 600;
        color: var(--primary-text);
      }
    `,
  ],
})
export class CardComponent {
  @Input() title: string | null = null;
}
