import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/auth.guards';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/catalog/catalog.component').then(module => module.CatalogComponent),
    title: 'Shop | Karan Commerce'
  },
  {
    path: 'cart',
    loadComponent: () => import('./features/cart/cart.component').then(module => module.CartComponent),
    title: 'Cart | Karan Commerce'
  },
  {
    path: 'checkout',
    canActivate: [authGuard],
    loadComponent: () => import('./features/checkout/checkout.component').then(module => module.CheckoutComponent),
    title: 'Checkout | Karan Commerce'
  },
  {
    path: 'account',
    canActivate: [authGuard],
    loadComponent: () => import('./features/account/account.component').then(module => module.AccountComponent),
    title: 'My Account | Karan Commerce'
  },
  {
    path: 'track',
    loadComponent: () => import('./features/tracking/tracking.component').then(module => module.TrackingComponent),
    title: 'Track shipment | Karan Commerce'
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/admin.component').then(module => module.AdminComponent),
    title: 'Operations | Karan Commerce'
  },
  { path: '**', redirectTo: '' }
];
