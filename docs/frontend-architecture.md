# Frontend Architecture

Application: Angular 18 enterprise frontend for the Enterprise Service Ticketing System.

## Folder Structure

```text
frontend/
|-- angular.json
|-- package.json
|-- tailwind.config.js
|-- postcss.config.js
|-- src/
|   |-- main.ts
|   |-- styles.scss
|   |-- environments/
|   |   |-- environment.ts
|   |   `-- environment.prod.ts
|   `-- app/
|       |-- app.config.ts
|       |-- app.routes.ts
|       |-- app.component.ts
|       |-- core/
|       |   |-- auth/
|       |   |-- guards/
|       |   |-- interceptors/
|       |   |-- models/
|       |   `-- services/
|       |-- features/
|       |   |-- auth/
|       |   |-- dashboard/
|       |   |-- tickets/
|       |   |-- admin/
|       |   `-- service-manager/
|       |-- layout/
|       `-- shared/
|           |-- components/
|           |   |-- chart-card/
|           |   |-- empty-state/
|           |   |-- metric-card/
|           |   |-- page-header/
|           |   |-- status-chip/
|           |-- directives/
|           |-- dialogs/
|           `-- pipes/
```

## Routing Structure

The app uses Angular standalone APIs and lazy route files:

- `/auth/login`: public login page.
- `/auth/register`: public registration entry point.
- `/dashboard`: authenticated dashboard.
- `/tickets`: authenticated ticket queue.
- `/tickets/new`: authenticated ticket creation.
- `/tickets/:ticketId/edit`: authenticated ticket editing.
- `/tickets/:ticketId`: authenticated ticket details.
- `/admin`: `ADMIN` role only.
- `/service-manager`: `SERVICE_MANAGER` or `ADMIN`.

Guards:

- `authGuard`: blocks unauthenticated access.
- `noAuthGuard`: redirects authenticated users away from login/register.
- `roleGuard`: checks route `data.roles`.

## Module Structure

Angular 18 standalone route modules are used instead of legacy NgModules:

- `core`: singleton services, JWT interceptor, guards, global models.
- `shared`: reusable presentational components, directives, and pipes.
- `layout`: authenticated shell and navigation.
- `features/auth`: login and registration flows.
- `features/dashboard`: KPI metrics, Chart.js analytics, trends, and activity feed.
- `features/tickets`: ticket list, filters, create, edit, detail, timeline, comments, attachments, and ticket API service.
- `features/admin`: admin-only governance and audit workflow surface.
- `features/service-manager`: assignment, SLA, and escalation service workspace.

## Shared Component Strategy

Shared components are stateless and reusable:

- `PageHeaderComponent`: consistent page heading and action slot.
- `LoadingStateComponent`: standardized async loading wrapper.
- `StatusChipComponent`: consistent ticket status and priority badges.
- `MetricCardComponent`: reusable KPI surface for dashboards.
- `ChartCardComponent`: reusable Chart.js container with responsive sizing.
- `EmptyStateComponent`: reusable no-data and error recovery surface.
- `ConfirmDialogComponent`: reusable Material dialog shell.

Shared components should not call APIs directly. They receive data through inputs and emit events when interaction is required.

## State Management Strategy

Current baseline:

- RxJS for API request streams.
- Angular signals for local auth/session state.
- Feature services own feature-level API orchestration.
- Components stay thin and use typed DTOs from `core/models`.
- JWT, loading, and error handling are centralized in HTTP interceptors.
- Ticket search, history, comments, and attachments use POST endpoints to match the security rule.

Scaling path:

- Add facade services per feature when workflows become multi-step.
- Add signal stores for dashboard metrics, ticket filters, and assignment queues.
- Introduce NgRx only if cross-feature state, optimistic updates, or complex entity caching becomes necessary.

## Environment Management

- `environment.ts`: local API target, `http://localhost:8080/api/v1`.
- `environment.prod.ts`: relative API target, `/api/v1`, suitable for reverse proxy deployment.
- Token storage keys are centralized per environment.

## Run Commands

```powershell
cd frontend
npm install
npm start
```

Production build:

```powershell
cd frontend
npm run build
```

Unit tests:

```powershell
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```
