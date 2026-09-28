import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <header class="page-header">
      <div>
        <p class="eyebrow">JewelVaultERP</p>
        <h1>{{ title }}</h1>
      </div>
      @if (subtitle) {
        <p class="subtitle">{{ subtitle }}</p>
      }
    </header>
  `,
  styles: [
    `
      .page-header {
        display: flex;
        justify-content: space-between;
        align-items: end;
        gap: 1.25rem;
        margin-bottom: 1.25rem;
      }
      .eyebrow {
        margin: 0 0 0.3rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        font-size: 0.7rem;
        color: var(--gold);
        font-weight: 600;
      }
      h1 {
        margin: 0;
        color: var(--primary-text);
        font-size: 1.75rem;
        line-height: 1.2;
        font-weight: 600;
      }
      .subtitle {
        margin: 0;
        color: var(--secondary-text);
        max-width: 32rem;
        font-size: 0.9rem;
        line-height: 1.45;
      }
      @media (max-width: 640px) {
        .page-header { display: grid; gap: 0.45rem; }
        h1 { font-size: 1.5rem; }
      }
    `,
  ],
})
export class PageHeaderComponent {
  @Input() title = 'Dashboard';
  @Input() subtitle = '';
}
