import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { ModuleCode } from '../models/admin.models';
import { AccessService } from '../services/access.service';

export const moduleAccessGuard: CanActivateFn = (route) => {
  const accessService = inject(AccessService);
  const router = inject(Router);
  const moduleCode = route.data['module'] as ModuleCode | undefined;

  if (!moduleCode) {
    return true;
  }

  const allowed = () => accessService.canAccess(moduleCode) || router.createUrlTree(['/dashboard']);
  if (accessService.loaded()) {
    return allowed();
  }

  return accessService.load().pipe(
    map(allowed),
    catchError(() => of(router.createUrlTree(['/auth/login'])))
  );
};
