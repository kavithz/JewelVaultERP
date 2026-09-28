import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  route?: string;
}

@Component({
  selector: 'app-breadcrumbs',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <nav class="breadcrumbs" aria-label="Breadcrumb">
      @for (item of items; track item.label; let last = $last) {
        <span class="crumb">
          @if (item.route && !last) {
            <a [routerLink]="item.route">{{ item.label }}</a>
          } @else {
            <span>{{ item.label }}</span>
          }
        </span>
        @if (!last) {
          <span class="separator">/</span>
        }
      }
    </nav>
  `,
  styles: [
    `
      .breadcrumbs {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.4rem;
        font-size: 0.8rem;
        color: var(--secondary-text);
      }
      .crumb a { color: var(--secondary-text); text-decoration: none; }
      .crumb a:hover { color: var(--primary-text); }
      .separator { opacity: 0.7; }
    `,
  ],
})
export class BreadcrumbsComponent {
  @Input() items: BreadcrumbItem[] = [{ label: 'Dashboard', route: '/dashboard' }];
}
