import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { RoleCode } from '../models/auth.models';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = (route.data['roles'] ?? []) as RoleCode[];
  const userRole = authService.currentUser()?.role;

  if (!allowedRoles.length || (userRole && allowedRoles.includes(userRole))) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
