import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <article class="kpi-card" [ngClass]="tone">
      <div class="meta">
        <span class="kpi-icon" aria-hidden="true"><svg><use [attr.href]="'/icons.svg#' + icon"></use></svg></span>
        @if (trend) { <span class="trend" [ngClass]="trendTone">{{ trend }}</span> }
      </div>
      <span class="label">{{ label }}</span>
      <div class="value">{{ value }}</div>
    </article>
  `,
  styles: [
    `
      :host { display: block; }
      .kpi-card {
        background: var(--surface);
        border: 1px solid var(--border-color);
        border-radius: 0.75rem;
        padding: 1rem 1.05rem;
        box-shadow: var(--shadow-soft);
        border-top: 3px solid var(--primary);
      }
      .kpi-card.gold { border-top-color: var(--gold); }
      .kpi-card.emerald { border-top-color: var(--emerald); }
      .kpi-card.amethyst { border-top-color: var(--amethyst); }
      .meta { display: flex; align-items: center; justify-content: space-between; min-height: 2rem; margin-bottom: 0.45rem; }
      .kpi-icon { display: grid; width: 2rem; height: 2rem; place-items: center; border-radius: 0.55rem; background: var(--primary-light); color: var(--primary); }
      .kpi-card.gold .kpi-icon { background: var(--gold-light); color: #7a5a00; }
      .kpi-card.emerald .kpi-icon { background: var(--emerald-light); color: var(--emerald); }
      .kpi-card.amethyst .kpi-icon { background: var(--amethyst-light); color: var(--amethyst); }
      .kpi-icon svg { width: 1.1rem; height: 1.1rem; fill: none; stroke: currentColor; stroke-width: 1.7; stroke-linecap: round; stroke-linejoin: round; }
      .label { display: block; color: var(--secondary-text); font-size: 0.82rem; font-weight: 500; }
      .trend {
        font-size: 0.7rem;
        font-weight: 500;
        border-radius: 0.35rem;
        padding: 0.2rem 0.4rem;
      }
      .trend.positive { background: var(--emerald-light); color: var(--emerald); }
      .trend.neutral { background: var(--surface-soft); color: var(--secondary-text); border: 1px solid var(--border-color); }
      .value {
        margin-top: 0.35rem;
        color: var(--primary-text);
        font-size: 1.55rem;
        line-height: 1.2;
        font-weight: 600;
        overflow-wrap: anywhere;
      }
    `,
  ],
})
export class KpiCardComponent {
  @Input() label = 'Revenue';
  @Input() value = '$0.00';
  @Input() trend = '';
  @Input() trendTone: 'positive' | 'neutral' = 'positive';
  @Input() tone: 'blue' | 'gold' | 'emerald' | 'amethyst' = 'blue';
  @Input() icon = 'gem';
}
