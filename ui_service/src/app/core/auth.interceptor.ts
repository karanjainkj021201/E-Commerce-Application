import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);

  const isPublicRequest =
      request.url.startsWith('/api/catalog/') ||
      (
          request.url.startsWith('/api/inventory/products/') &&
          request.url.includes('/availability')
      ) ||
      request.url.startsWith('/api/shipments/track/');

  // Public endpoints must not receive a bearer token.
  if (
      !request.url.startsWith('/api') ||
      isPublicRequest ||
      !auth.authenticated()
  ) {
    return next(request);
  }

  return from(auth.ensureFreshToken()).pipe(
      switchMap(token =>
          next(
              token
                  ? request.clone({
                    setHeaders: {
                      Authorization: `Bearer ${token}`
                    }
                  })
                  : request
          )
      )
  );
};