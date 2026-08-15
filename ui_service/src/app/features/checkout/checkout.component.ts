import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { CreateOrderRequest, Order } from '../../core/models';

@Component({
  selector: 'app-checkout',
  imports: [FormsModule, CurrencyPipe, RouterLink],
  templateUrl: './checkout.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  readonly cart = inject(CartService);
  readonly submitting = signal(false);
  readonly error = signal('');
  readonly createdOrder = signal<Order | undefined>(undefined);

  address = {
    customerName: '',
    customerEmail: '',
    customerPhone: '',
    line1: '',
    line2: '',
    city: '',
    state: '',
    pincode: '',
    country: 'India'
  };

  ngOnInit(): void {
    const keycloakProfile = this.auth.getProfile();
    this.address.customerName = [keycloakProfile?.firstName, keycloakProfile?.lastName].filter(Boolean).join(' ');
    this.address.customerEmail = keycloakProfile?.email ?? '';

    this.api.getProfile().subscribe({
      next: profile => {
        this.address.customerName = profile.name;
        this.address.customerEmail = profile.email;
        this.address.customerPhone = profile.phone;
      },
      error: () => {
        // A user-service profile is optional at checkout; Keycloak identity still owns the order.
      }
    });
  }

  placeOrder(form: NgForm): void {
    if (form.invalid || this.cart.items().length === 0) {
      form.control.markAllAsTouched();
      return;
    }

    const request: CreateOrderRequest = {
      items: this.cart.items().map(item => ({ productId: item.product.id, quantity: item.quantity })),
      shippingAddress: { ...this.address },
      paymentMethod: 'GOOGLE_PAY',
      shippingFee: 50
    };

    this.error.set('');
    this.submitting.set(true);
    this.api.createOrder(request)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: order => {
          this.createdOrder.set(order);
          this.cart.clear();
        },
        error: (response: HttpErrorResponse) => this.error.set(
          response.error?.message ?? response.error?.error ?? 'The order could not be placed. Check product status, stock, and service logs.'
        )
      });
  }
}
