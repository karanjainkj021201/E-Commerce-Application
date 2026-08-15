import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Category, Order, Page, Payment, Product, Shipment, Stock, UserProfile } from '../../core/models';

type AdminTab = 'overview' | 'catalog' | 'inventory' | 'orders' | 'payments' | 'shipping' | 'users';

const emptyPage = <T>(): Page<T> => ({
  content: [], number: 0, size: 100, totalElements: 0, totalPages: 0, first: true, last: true
});

@Component({
  selector: 'app-admin',
  imports: [CurrencyPipe, DatePipe, FormsModule],
  templateUrl: './admin.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly activeTab = signal<AdminTab>('overview');
  readonly loading = signal(true);
  readonly message = signal('');
  readonly categories = signal<Category[]>([]);
  readonly products = signal<Product[]>([]);
  readonly stocks = signal<Stock[]>([]);
  readonly orders = signal<Order[]>([]);
  readonly payments = signal<Payment[]>([]);
  readonly shipments = signal<Shipment[]>([]);
  readonly users = signal<UserProfile[]>([]);

  categoryForm = { name: '', code: '', description: '' };
  productForm = { sku: '', name: '', description: '', price: 0, currency: 'INR', imageUrl: '', categoryId: 0 };
  stockForm = { productId: 0, sku: '', productName: '', warehouseCode: 'WH-DEFAULT', quantity: 0, reason: 'Initial UI stock' };

  ngOnInit(): void {
    this.loadAll();
  }

  setTab(tab: AdminTab): void {
    this.activeTab.set(tab);
    this.message.set('');
  }

  loadAll(): void {
    this.loading.set(true);
    forkJoin({
      categories: this.api.adminCategories().pipe(catchError(() => of([] as Category[]))),
      products: this.api.adminProducts().pipe(catchError(() => of(emptyPage<Product>()))),
      stocks: this.api.adminStocks().pipe(catchError(() => of(emptyPage<Stock>()))),
      orders: this.api.adminOrders().pipe(catchError(() => of(emptyPage<Order>()))),
      payments: this.api.adminPayments().pipe(catchError(() => of(emptyPage<Payment>()))),
      shipments: this.api.adminShipments().pipe(catchError(() => of(emptyPage<Shipment>()))),
      users: this.api.adminUsers().pipe(catchError(() => of(emptyPage<UserProfile>())))
    }).subscribe(result => {
      this.categories.set(result.categories);
      this.products.set(result.products.content);
      this.stocks.set(result.stocks.content);
      this.orders.set(result.orders.content);
      this.payments.set(result.payments.content);
      this.shipments.set(result.shipments.content);
      this.users.set(result.users.content);
      this.loading.set(false);
    });
  }

  createCategory(form: NgForm): void {
    if (form.invalid) return;
    this.api.adminCreateCategory(this.categoryForm).subscribe({
      next: category => {
        this.categories.update(categories => [...categories, category]);
        this.categoryForm = { name: '', code: '', description: '' };
        form.resetForm(this.categoryForm);
        this.success(`Category ${category.name} created.`);
      },
      error: () => this.failure('Category creation failed. The name and code must be unique.')
    });
  }

  toggleCategory(category: Category): void {
    this.api.adminToggleCategory(category.id, !category.active).subscribe({
      next: updated => {
        this.categories.update(items => items.map(item => item.id === updated.id ? updated : item));
        this.success(`${updated.name} is now ${updated.active ? 'active' : 'inactive'}.`);
      },
      error: () => this.failure('Category status could not be changed.')
    });
  }

  createProduct(form: NgForm): void {
    if (form.invalid || !this.productForm.categoryId) return;
    this.api.adminCreateProduct(this.productForm).subscribe({
      next: product => {
        this.products.update(products => [product, ...products]);
        this.productForm = { sku: '', name: '', description: '', price: 0, currency: 'INR', imageUrl: '', categoryId: 0 };
        form.resetForm(this.productForm);
        this.success(`Product ${product.name} created. Activate it, then add inventory.`);
      },
      error: () => this.failure('Product creation failed. Check the SKU, category, price, and required fields.')
    });
  }

  setProductStatus(product: Product, status: string): void {
    this.api.adminUpdateProductStatus(product.id, status).subscribe({
      next: updated => {
        this.products.update(products => products.map(item => item.id === updated.id ? updated : item));
        this.success(`${updated.name} is now ${updated.status}.`);
      },
      error: () => this.failure('Product status update failed.')
    });
  }

  useProductForStock(product: Product): void {
    this.stockForm.productId = product.id;
    this.stockForm.sku = product.sku;
    this.stockForm.productName = product.name;
    this.setTab('inventory');
  }

  createStock(form: NgForm): void {
    if (form.invalid || !this.stockForm.productId) return;
    this.api.adminCreateStock(this.stockForm).subscribe({
      next: stock => {
        this.stocks.update(stocks => [stock, ...stocks]);
        this.stockForm = { productId: 0, sku: '', productName: '', warehouseCode: 'WH-DEFAULT', quantity: 0, reason: 'Initial UI stock' };
        form.resetForm(this.stockForm);
        this.success(`Stock record for ${stock.productName} created.`);
      },
      error: () => this.failure('Stock creation failed. The product/warehouse combination may already exist.')
    });
  }

  adjustStock(stock: Stock, operation: 'increase' | 'decrease' | 'set'): void {
    const rawQuantity = window.prompt(`${operation === 'set' ? 'Set total' : operation} quantity for ${stock.productName}:`);
    if (rawQuantity === null) return;
    const quantity = Number(rawQuantity);
    if (!Number.isInteger(quantity) || quantity < (operation === 'set' ? 0 : 1)) {
      this.failure('Enter a valid whole-number quantity.');
      return;
    }
    const reason = window.prompt('Reason for this adjustment:', 'Admin UI adjustment') ?? 'Admin UI adjustment';
    const request = operation === 'set'
      ? this.api.adminSetStock(stock.id, quantity, reason)
      : this.api.adminAdjustStock(stock.id, operation, quantity, reason);
    request.subscribe({
      next: updated => {
        this.stocks.update(stocks => stocks.map(item => item.id === updated.id ? updated : item));
        this.success(`Stock updated. ${updated.availableQuantity} units are available.`);
      },
      error: () => this.failure('Stock adjustment failed. Reserved units cannot be removed.')
    });
  }

  updateOrder(order: Order): void {
    const status = window.prompt('New order status:', order.status)?.trim().toUpperCase();
    if (!status) return;
    const reason = window.prompt('Reason:', 'Admin status update') ?? 'Admin status update';
    this.api.adminUpdateOrderStatus(order.id, status, reason).subscribe({
      next: updated => {
        this.orders.update(orders => orders.map(item => item.id === updated.id ? updated : item));
        this.success(`${updated.orderNumber} is now ${updated.status}.`);
      },
      error: () => this.failure('Order transition was rejected. Use a valid state transition.')
    });
  }

  paymentSuccess(payment: Payment): void {
    const gatewayReference = window.prompt('Gateway reference:', `GATEWAY-${Date.now()}`);
    if (gatewayReference === null) return;
    this.api.adminMockPaymentSuccess(payment.paymentReference, gatewayReference).subscribe({
      next: updated => {
        this.payments.update(items => items.map(item => item.id === updated.id ? updated : item));
        this.success(`${updated.paymentReference} marked successful. Kafka will confirm the order.`);
      },
      error: () => this.failure('Mock payment success failed.')
    });
  }

  paymentFailure(payment: Payment): void {
    const reason = window.prompt('Failure reason:', 'Customer declined payment');
    if (!reason) return;
    this.api.adminMockPaymentFailure(payment.paymentReference, reason).subscribe({
      next: updated => {
        this.payments.update(items => items.map(item => item.id === updated.id ? updated : item));
        this.success(`${updated.paymentReference} marked failed.`);
      },
      error: () => this.failure('Mock payment failure failed.')
    });
  }

  refund(payment: Payment): void {
    const rawAmount = window.prompt('Refund amount:', String(payment.amount));
    if (!rawAmount) return;
    const amount = Number(rawAmount);
    const reason = window.prompt('Refund reason:', 'Admin refund') ?? 'Admin refund';
    this.api.adminRefund(payment.paymentReference, amount, reason).subscribe({
      next: () => {
        this.success(`Refund requested for ${payment.paymentReference}.`);
        this.loadAll();
      },
      error: () => this.failure('Refund failed. Verify that the payment succeeded and the amount is refundable.')
    });
  }

  assignTracking(shipment: Shipment): void {
    const carrier = window.prompt('Carrier:', shipment.carrier || 'Blue Dart');
    if (!carrier) return;
    const trackingNumber = window.prompt('Tracking number:', shipment.trackingNumber || `TRACK-${shipment.id}`);
    if (!trackingNumber) return;
    this.api.adminUpdateShipmentDetails(shipment.id, carrier, trackingNumber).subscribe({
      next: updated => {
        this.shipments.update(items => items.map(item => item.id === updated.id ? updated : item));
        this.success(`Tracking assigned to ${updated.shipmentNumber}.`);
      },
      error: () => this.failure('Carrier and tracking number could not be updated.')
    });
  }

  updateShipment(shipment: Shipment): void {
    const status = window.prompt('New shipment status (IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, CANCELLED):', shipment.status)?.trim().toUpperCase();
    if (!status) return;
    const note = window.prompt('Tracking note:', 'Status updated by operations') ?? 'Status updated by operations';
    this.api.adminUpdateShipmentStatus(shipment.id, status, note).subscribe({
      next: updated => {
        this.shipments.update(items => items.map(item => item.id === updated.id ? updated : item));
        this.success(`${updated.shipmentNumber} is now ${updated.status}.`);
      },
      error: () => this.failure('Shipment transition failed. Assign tracking before dispatch and follow the valid status order.')
    });
  }

  updateUser(user: UserProfile): void {
    const status = window.prompt('New user status (ACTIVE, INACTIVE, BLOCKED):', user.status)?.trim().toUpperCase();
    if (!status) return;
    this.api.adminUpdateUserStatus(user.id, status).subscribe({
      next: updated => {
        this.users.update(items => items.map(item => item.id === updated.id ? updated : item));
        this.success(`${updated.name} is now ${updated.status}.`);
      },
      error: () => this.failure('User status update failed.')
    });
  }

  private success(message: string): void {
    this.message.set(message);
  }

  private failure(message: string): void {
    this.message.set(message);
  }
}
