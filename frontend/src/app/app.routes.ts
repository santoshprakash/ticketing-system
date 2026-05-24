import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { moduleAccessGuard } from './core/guards/module-access.guard';
import { noAuthGuard } from './core/guards/no-auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [noAuthGuard],
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES)
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: 'dashboard',
        canActivate: [moduleAccessGuard],
        data: { module: 'DASHBOARD' },
        loadChildren: () => import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES)
      },
      {
        path: 'tickets',
        canActivate: [moduleAccessGuard],
        data: { module: 'TICKETS' },
        loadChildren: () => import('./features/tickets/tickets.routes').then((m) => m.TICKETS_ROUTES)
      },
      {
        path: 'admin',
        canActivate: [roleGuard, moduleAccessGuard],
        data: { roles: ['ADMIN'], module: 'ADMIN' },
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES)
      },
      {
        path: 'service-manager',
        canActivate: [roleGuard, moduleAccessGuard],
        data: { roles: ['SERVICE_MANAGER', 'ADMIN'], module: 'SERVICE_MANAGER' },
        loadChildren: () => import('./features/service-manager/service-manager.routes').then((m) => m.SERVICE_MANAGER_ROUTES)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
