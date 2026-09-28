import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [NgClass],
  template: `
    <span class="status-badge" [ngClass]="tone">
      {{ label }}
    </span>
  `,
  styles: [
    `
      .status-badge {
        display: inline-flex;
        align-items: center;
        padding: 0.25rem 0.5rem;
        border-radius: 0.4rem;
        font-size: 0.72rem;
        font-weight: 500;
        letter-spacing: 0;
        white-space: nowrap;
      }
      .status-badge.success { background: var(--emerald-light); color: var(--emerald); }
      .status-badge.warning { background: var(--gold-light); color: #775800; }
      .status-badge.danger { background: var(--ruby-light); color: var(--ruby); }
      .status-badge.neutral { background: var(--surface-soft); color: var(--secondary-text); border: 1px solid var(--border-color); }
    `,
  ],
})
export class StatusBadgeComponent {
  @Input() label = 'Active';
  @Input() tone: 'success' | 'warning' | 'danger' | 'neutral' = 'success';
}
