import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="not-found" aria-labelledby="not-found-heading">
      <p class="eyebrow">404</p>
      <h1 id="not-found-heading">Page not found</h1>
      <p>This path does not have a page in the current application.</p>
      <a routerLink="/dashboard">Return to Dashboard</a>
    </section>
  `,
  styles: [`
    :host { display: block; min-width: 0; }
    .not-found { max-width: 38rem; margin: 3rem auto; padding: 1.5rem; border: 1px solid var(--border-color); border-radius: 0.5rem; background: var(--surface); }
    .eyebrow { margin: 0 0 0.5rem; color: var(--gold); font-size: 0.7rem; font-weight: 600; text-transform: uppercase; }
    h1 { margin: 0; color: var(--primary-text); font-size: 1.6rem; font-weight: 600; }
    .not-found > p:not(.eyebrow) { color: var(--secondary-text); }
    a { color: var(--accent-strong); }
    a:focus-visible { outline: 2px solid var(--accent); outline-offset: 2px; }
  `],
})
export class NotFoundPageComponent {}