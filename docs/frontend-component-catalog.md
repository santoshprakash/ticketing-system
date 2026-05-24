# Angular Frontend Component Catalog

Application: Enterprise Service Ticketing System  
Stack: Angular 18, Angular Material, Tailwind CSS, SCSS, RxJS, HttpClient, Chart.js, Jasmine/Karma.

## Architecture Summary

- Standalone Angular components with lazy-loaded route files.
- `core` owns singleton services, guards, interceptors, and DTO models.
- `features` owns business pages and feature services.
- `shared` owns reusable presentational components and dialogs.
- `layout` owns the authenticated shell and responsive navigation.
- API access is centralized through `ApiService`; feature services expose typed methods.
- Authentication, loading, errors, and JWT attachment are handled globally through interceptors.
- RWD strategy: off-canvas navigation below `960px`, stacked page headers/actions below `640px`, responsive grids, mobile-safe forms, and horizontally scrollable enterprise tables.

## Routing Configuration

| Route | Module | Guard | Component |
|---|---|---|---|
| `/auth/login` | `features/auth` | `noAuthGuard` | `LoginPageComponent` |
| `/auth/register` | `features/auth` | `noAuthGuard` | `RegisterPageComponent` |
| `/auth/forgot-password` | `features/auth` | `noAuthGuard` | `ForgotPasswordPageComponent` |
| `/auth/reset-password` | `features/auth` | `noAuthGuard` | `ResetPasswordPageComponent` |
| `/dashboard` | `features/dashboard` | `authGuard` | `DashboardPageComponent` |
| `/tickets` | `features/tickets` | `authGuard` | `TicketListPageComponent` |
| `/tickets/new` | `features/tickets` | `authGuard` | `TicketCreatePageComponent` |
| `/tickets/:ticketId` | `features/tickets` | `authGuard` | `TicketDetailPageComponent` |
| `/tickets/:ticketId/edit` | `features/tickets` | `authGuard` | `TicketEditPageComponent` |
| `/admin` | `features/admin` | `authGuard`, `roleGuard: ADMIN` | `AdminPageComponent` |
| `/service-manager` | `features/service-manager` | `authGuard`, `roleGuard: SERVICE_MANAGER/ADMIN` | `ServiceManagerPageComponent` |

Route files:

- `frontend/src/app/app.routes.ts`
- `frontend/src/app/features/auth/auth.routes.ts`
- `frontend/src/app/features/dashboard/dashboard.routes.ts`
- `frontend/src/app/features/tickets/tickets.routes.ts`
- `frontend/src/app/features/admin/admin.routes.ts`
- `frontend/src/app/features/service-manager/service-manager.routes.ts`

## Pages And Components

| Folder path | Component | HTML template | SCSS styles | TypeScript logic | Purpose |
|---|---|---|---|---|---|
| `src/app/layout/shell` | `ShellComponent` | `shell.component.html` | `shell.component.scss` | `shell.component.ts` | Authenticated app shell, RWD sidebar drawer, top bar, theme toggle, logout |
| `src/app/features/auth/pages` | `LoginPageComponent` | `login-page.component.html` | `auth-page.component.scss` | `login-page.component.ts` | Premium SaaS login page with validation and loading state |
| `src/app/features/auth/pages` | `RegisterPageComponent` | `register-page.component.html` | `auth-page.component.scss` | `register-page.component.ts` | Customer registration with reactive validation |
| `src/app/features/auth/pages` | `ForgotPasswordPageComponent` | `forgot-password-page.component.html` | `auth-page.component.scss` | `forgot-password-page.component.ts` | Password recovery request flow |
| `src/app/features/auth/pages` | `ResetPasswordPageComponent` | `reset-password-page.component.html` | `auth-page.component.scss` | `reset-password-page.component.ts` | Token-based password reset flow |
| `src/app/features/dashboard/pages` | `DashboardPageComponent` | `dashboard-page.component.html` | `dashboard-page.component.scss` | `dashboard-page.component.ts` | KPI dashboard, charts, recent activity, performance panels |
| `src/app/features/tickets/pages` | `TicketListPageComponent` | `ticket-list-page.component.html` | `ticket-list-page.component.scss` | `ticket-list-page.component.ts` | Searchable, filterable, pageable ticket queue |
| `src/app/features/tickets/pages` | `TicketCreatePageComponent` | `ticket-create-page.component.html` | `ticket-form-page.component.scss` | `ticket-create-page.component.ts` | Validated ticket creation form |
| `src/app/features/tickets/pages` | `TicketEditPageComponent` | `ticket-edit-page.component.html` | `ticket-form-page.component.scss` | `ticket-edit-page.component.ts` | Validated ticket edit form with route data loading |
| `src/app/features/tickets/pages` | `TicketDetailPageComponent` | `ticket-detail-page.component.html` | `ticket-detail-page.component.scss` | `ticket-detail-page.component.ts` | Ticket summary, SLA facts, timeline, comments, attachments |
| `src/app/features/tickets/components/ticket-timeline` | `TicketTimelineComponent` | `ticket-timeline.component.html` | `ticket-timeline.component.scss` | `ticket-timeline.component.ts` | Reusable ticket history timeline |
| `src/app/features/tickets/components/ticket-comments` | `TicketCommentsComponent` | `ticket-comments.component.html` | `ticket-comments.component.scss` | `ticket-comments.component.ts` | Comment thread and add-comment form |
| `src/app/features/tickets/components/ticket-attachments` | `TicketAttachmentsComponent` | `ticket-attachments.component.html` | `ticket-attachments.component.scss` | `ticket-attachments.component.ts` | Attachment list and upload event surface |
| `src/app/features/admin/pages` | `AdminPageComponent` | `admin-page.component.html` | `admin-page.component.scss` | `admin-page.component.ts` | Admin governance and audit workspace |
| `src/app/features/service-manager/pages` | `ServiceManagerPageComponent` | `service-manager-page.component.html` | `service-manager-page.component.scss` | `service-manager-page.component.ts` | Assignment, SLA, and escalation workspace |
| `src/app/shared/components/page-header` | `PageHeaderComponent` | Inline | Inline | `page-header.component.ts` | Reusable responsive page title/action header |
| `src/app/shared/components/status-chip` | `StatusChipComponent` | Inline | Tailwind classes | `status-chip.component.ts` | Status and priority badge rendering |
| `src/app/shared/components/loading-state` | `LoadingStateComponent` | Inline | Inline | `loading-state.component.ts` | Reusable async loading state |
| `src/app/shared/components/empty-state` | `EmptyStateComponent` | `empty-state.component.html` | `empty-state.component.scss` | `empty-state.component.ts` | Reusable empty/no-data state |
| `src/app/shared/components/metric-card` | `MetricCardComponent` | `metric-card.component.html` | `metric-card.component.scss` | `metric-card.component.ts` | Reusable KPI card |
| `src/app/shared/components/chart-card` | `ChartCardComponent` | `chart-card.component.html` | `chart-card.component.scss` | `chart-card.component.ts` | Reusable Chart.js card wrapper |
| `src/app/shared/dialogs/confirm-dialog` | `ConfirmDialogComponent` | `confirm-dialog.component.html` | `confirm-dialog.component.scss` | `confirm-dialog.component.ts` | Reusable confirmation dialog |

## Services

| Path | Service | Responsibility |
|---|---|---|
| `src/app/core/services/api.service.ts` | `ApiService` | Typed HttpClient wrapper with API base URL and params |
| `src/app/core/auth/auth.service.ts` | `AuthService` | Login, register, forgot/reset password, logout, session state |
| `src/app/core/auth/token-storage.service.ts` | `TokenStorageService` | Access token, refresh token, and authenticated user persistence |
| `src/app/core/services/loading.service.ts` | `LoadingService` | Global loading signal |
| `src/app/core/services/notification.service.ts` | `NotificationService` | Material snackbar success/error notifications |
| `src/app/core/services/theme.service.ts` | `ThemeService` | Light/dark theme state |
| `src/app/features/tickets/services/tickets.service.ts` | `TicketsService` | Ticket search, create, update, assign, status, resolve, reopen, comments, history, attachments |
| `src/app/features/dashboard/services/dashboard.service.ts` | `DashboardService` | Dashboard metrics, charts, recent activity data |

## Models And Interfaces

| Path | Interfaces/types |
|---|---|
| `src/app/core/models/auth.models.ts` | `RoleCode`, `AuthUser`, `LoginRequest`, `RegisterRequest`, `AuthResponse` |
| `src/app/core/models/ticket.models.ts` | `TicketStatus`, `TicketPriority`, `TicketHistoryAction`, `Ticket`, `CreateTicketRequest`, `UpdateTicketRequest`, `AssignTicketRequest`, `ChangeTicketStatusRequest`, `AddCommentRequest`, `TicketFilter`, `Page<T>`, `TicketComment`, `TicketAttachment`, `TicketHistory` |
| `src/app/features/dashboard/services/dashboard.service.ts` | `DashboardMetric`, `DashboardActivity`, `DashboardOverview` |

## API Integration Examples

Authentication:

```ts
this.authService.login({
  email: 'customer.acme@ticketing.local',
  password: 'Password123!'
});
```

Ticket search using POST:

```ts
this.ticketsService.search(
  { status: 'OPEN', priority: 'HIGH', search: 'portal' },
  0,
  10
);
```

Ticket creation:

```ts
this.ticketsService.create({
  title: 'Cannot access customer portal',
  description: 'Customer cannot access the portal after password reset.',
  priority: 'HIGH',
  category: 'ACCESS'
});
```

Ticket comment:

```ts
this.ticketsService.addComment(ticketId, {
  comment: 'Customer confirmed the issue is still active.',
  internal: false
});
```

Attachment upload:

```ts
this.ticketsService.uploadAttachment(ticketId, file);
```

## Guards And Interceptors

| Path | Name | Responsibility |
|---|---|---|
| `src/app/core/guards/auth.guard.ts` | `authGuard` | Blocks unauthenticated feature routes |
| `src/app/core/guards/no-auth.guard.ts` | `noAuthGuard` | Redirects authenticated users away from auth pages |
| `src/app/core/guards/role.guard.ts` | `roleGuard` | Enforces `ADMIN` and `SERVICE_MANAGER` route authorization |
| `src/app/core/interceptors/jwt.interceptor.ts` | `jwtInterceptor` | Adds `Authorization: Bearer <token>` |
| `src/app/core/interceptors/loading.interceptor.ts` | `loadingInterceptor` | Toggles global loading state |
| `src/app/core/interceptors/error.interceptor.ts` | `errorInterceptor` | Handles API errors and session redirects |

## Unit Tests

| Path | Coverage |
|---|---|
| `src/app/core/auth/token-storage.service.spec.ts` | Token and user persistence |
| `src/app/features/tickets/services/tickets.service.spec.ts` | Ticket API contract, including POST search |
| `src/app/shared/components/status-chip/status-chip.component.spec.ts` | Status/priority label rendering |

Run:

```powershell
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

## Global Styling And RWD

| Path | Responsibility |
|---|---|
| `src/styles.scss` | Tailwind, app tokens, page shell, surfaces, skeleton loading |
| `src/material-overrides.scss` | Enterprise Material form fields, input focus/error states, card radius |
| `tailwind.config.js` | Brand colors and content scan paths |
| `angular.json` | Style pipeline: app SCSS, Material theme, Material overrides |

Responsive breakpoints:

- `< 960px`: sidebar becomes overlay drawer.
- `< 768px`: page padding reduced.
- `< 640px`: page actions stack, filters stack, tables scroll.
- `< 480px`: compact page shell padding.

## Production Readiness Notes

- Components are standalone, lazy-loadable, and scoped by feature.
- DTOs are explicit and reusable across services/components.
- API calls are isolated in feature/core services.
- Material/Tailwind conflicts are handled through style order and global overrides.
- Ticket query endpoints use POST to match the security rule.
- Layout supports desktop, tablet, and mobile.
- Shared UI components avoid direct API calls and remain reusable.
