import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <header class="page-header">
      <div class="page-header__copy">
        <p>{{ eyebrow() }}</p>
        <h1>{{ title() }}</h1>
        <span>{{ description() }}</span>
      </div>
      <div class="page-header__actions">
        <ng-content />
      </div>
    </header>
  `,
  styles: [`
    .page-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.25rem;
    }

    .page-header__copy {
      min-width: 0;
    }

    p,
    h1,
    span {
      margin: 0;
    }

    p {
      color: var(--app-brand);
      font-size: 0.8rem;
      font-weight: 800;
      text-transform: uppercase;
    }

    h1 {
      margin-top: 0.25rem;
      color: var(--app-text);
      font-size: clamp(1.55rem, 5vw, 2rem);
      line-height: 1.15;
      font-weight: 800;
      letter-spacing: 0;
      overflow-wrap: anywhere;
    }

    span {
      display: block;
      max-width: 48rem;
      margin-top: 0.45rem;
      color: var(--app-subtle);
      font-size: 0.9rem;
      line-height: 1.55;
    }

    .page-header__actions {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 0.6rem;
      flex: 0 0 auto;
    }

    @media (max-width: 640px) {
      .page-header {
        align-items: stretch;
        flex-direction: column;
      }

      .page-header__actions,
      .page-header__actions ::ng-deep a,
      .page-header__actions ::ng-deep button {
        width: 100%;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageHeaderComponent {
  readonly eyebrow = input('Workspace');
  readonly title = input.required<string>();
  readonly description = input('');
}
