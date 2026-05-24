import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { TokenStorageService } from '../auth/token-storage.service';

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const notifications = inject(NotificationService);
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        tokenStorage.clear();
        notifications.error('Session expired. Please sign in again.');
        void router.navigate(['/auth/login']);
      } else if (error.status >= 500) {
        notifications.error('The service is temporarily unavailable.');
      } else if (error.status >= 400) {
        notifications.error(error.error?.message ?? 'The request could not be processed.');
      }

      return throwError(() => error);
    })
  );
};
