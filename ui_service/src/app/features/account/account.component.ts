import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Order, Page, Payment, Shipment, UserProfile } from '../../core/models';

type AccountTab = 'orders' | 'payments' | 'shipments' | 'profile';

const emptyPage = <T>(): Page<T> => ({
  content: [], number: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true
});

@Component({
  selector: 'app-account',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './account.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AccountComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  readonly activeTab = signal<AccountTab>('orders');
  readonly loading = signal(true);
  readonly message = signal('');
  readonly orders = signal<Order[]>([]);
  readonly payments = signal<Payment[]>([]);
  readonly shipments = signal<Shipment[]>([]);
  readonly pendingPaymentCount = computed(() => this.payments().filter(
    payment => ['PENDING', 'REDIRECT_CREATED'].includes(payment.status)
  ).length);
  readonly activeShipmentCount = computed(() => this.shipments().filter(
    shipment => !['DELIVERED', 'CANCELLED'].includes(shipment.status)
  ).length);
  readonly profileExists = signal(false);
  readonly savingProfile = signal(false);

  profileForm = { name: '', email: '', phone: '' };

  ngOnInit(): void {
    this.loadProfile();
    this.loadActivity();
  }

  setTab(tab: AccountTab): void {
    this.activeTab.set(tab);
    this.message.set('');
  }

  saveProfile(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }
    this.savingProfile.set(true);
    const request = this.profileExists()
      ? this.api.updateProfile(this.profileForm)
      : this.api.createProfile(this.profileForm);
    request.subscribe({
      next: profile => {
        this.applyProfile(profile);
        this.message.set('Profile saved successfully.');
        this.savingProfile.set(false);
      },
      error: () => {
        this.message.set('Profile could not be saved. Verify that the email is unique.');
        this.savingProfile.set(false);
      }
    });
  }

  cancel(order: Order): void {
    if (!window.confirm(`Cancel ${order.orderNumber}?`)) return;
    this.api.cancelOrder(order.id).subscribe({
      next: updated => {
        this.orders.update(orders => orders.map(item => item.id === updated.id ? updated : item));
        this.message.set(`${updated.orderNumber} was cancelled.`);
      },
      error: () => this.message.set('This order can no longer be cancelled.')
    });
  }

  openPayment(payment: Payment): void {
    if (!payment.gatewayPaymentUrl) {
      this.message.set('The payment gateway URL is not available yet. Refresh after the Payment service consumes the order event.');
      return;
    }
    window.open(payment.gatewayPaymentUrl, '_blank', 'noopener');
  }

  refresh(): void {
    this.loadActivity();
  }

  private loadProfile(): void {
    this.api.getProfile().subscribe({
      next: profile => this.applyProfile(profile),
      error: () => {
        const keycloakProfile = this.auth.getProfile();
        this.profileExists.set(false);
        this.profileForm = {
          name: [keycloakProfile?.firstName, keycloakProfile?.lastName].filter(Boolean).join(' '),
          email: keycloakProfile?.email ?? '',
          phone: ''
        };
      }
    });
  }

  private loadActivity(): void {
    this.loading.set(true);
    forkJoin({
      orders: this.api.getMyOrders().pipe(catchError(() => of(emptyPage<Order>()))),
      payments: this.api.getMyPayments().pipe(catchError(() => of(emptyPage<Payment>()))),
      shipments: this.api.getMyShipments().pipe(catchError(() => of(emptyPage<Shipment>())))
    }).subscribe(result => {
      this.orders.set(result.orders.content);
      this.payments.set(result.payments.content);
      this.shipments.set(result.shipments.content);
      this.loading.set(false);
    });
  }

  private applyProfile(profile: UserProfile): void {
    this.profileExists.set(true);
    this.profileForm = { name: profile.name, email: profile.email, phone: profile.phone };
  }
}
