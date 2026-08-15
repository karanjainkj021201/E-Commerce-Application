import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from './app.constants';
import {
  Category,
  CreateOrderRequest,
  InventoryAvailability,
  Order,
  Page,
  Payment,
  Product,
  Shipment,
  Stock,
  Tracking,
  UserProfile
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = APP_CONFIG.apiBaseUrl;

  getProducts(search = '', categoryId?: number, page = 0, size = 12): Observable<Page<Product>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search.trim()) params = params.set('search', search.trim());
    if (categoryId) params = params.set('categoryId', categoryId);
    return this.http.get<Page<Product>>(`${this.base}/catalog/products`, { params });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.base}/catalog/products/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.base}/catalog/categories`);
  }

  getAvailability(productId: number): Observable<InventoryAvailability> {
    return this.http.get<InventoryAvailability>(`${this.base}/inventory/products/${productId}/availability`);
  }

  track(trackingNumber: string): Observable<Tracking> {
    return this.http.get<Tracking>(`${this.base}/shipments/track/${encodeURIComponent(trackingNumber)}`);
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.base}/users/me`);
  }

  createProfile(body: Pick<UserProfile, 'name' | 'email' | 'phone'>): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.base}/users/me`, body);
  }

  updateProfile(body: Pick<UserProfile, 'name' | 'email' | 'phone'>): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.base}/users/me`, body);
  }

  createOrder(body: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(`${this.base}/orders`, body);
  }

  getMyOrders(page = 0, size = 20): Observable<Page<Order>> {
    return this.http.get<Page<Order>>(`${this.base}/orders/me`, { params: { page, size } });
  }

  cancelOrder(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.base}/orders/${id}/cancel`, {});
  }

  getMyPayments(page = 0, size = 20): Observable<Page<Payment>> {
    return this.http.get<Page<Payment>>(`${this.base}/payments/me`, { params: { page, size } });
  }

  getMyShipments(page = 0, size = 20): Observable<Page<Shipment>> {
    return this.http.get<Page<Shipment>>(`${this.base}/shipments/me`, { params: { page, size } });
  }

  adminUsers(): Observable<Page<UserProfile>> {
    return this.http.get<Page<UserProfile>>(`${this.base}/users`, { params: { page: 0, size: 100 } });
  }

  adminCreateUser(body: { name: string; email: string; phone: string }): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.base}/users`, body);
  }

  adminUpdateUserStatus(id: number, status: string): Observable<UserProfile> {
    return this.http.patch<UserProfile>(`${this.base}/users/${id}/status`, { status });
  }

  adminCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.base}/admin/categories`);
  }

  adminCreateCategory(body: { name: string; code: string; description: string }): Observable<Category> {
    return this.http.post<Category>(`${this.base}/admin/categories`, body);
  }

  adminToggleCategory(id: number, active: boolean): Observable<Category> {
    return this.http.patch<Category>(`${this.base}/admin/categories/${id}/active`, { active });
  }

  adminProducts(): Observable<Page<Product>> {
    return this.http.get<Page<Product>>(`${this.base}/admin/products`, { params: { page: 0, size: 100 } });
  }

  adminCreateProduct(body: {
    sku: string;
    name: string;
    description: string;
    price: number;
    currency: string;
    imageUrl?: string;
    categoryId: number;
  }): Observable<Product> {
    return this.http.post<Product>(`${this.base}/admin/products`, body);
  }

  adminUpdateProductStatus(id: number, status: string): Observable<Product> {
    return this.http.patch<Product>(`${this.base}/admin/products/${id}/status`, { status });
  }

  adminStocks(): Observable<Page<Stock>> {
    return this.http.get<Page<Stock>>(`${this.base}/admin/inventory/stocks`, { params: { page: 0, size: 100 } });
  }

  adminCreateStock(body: {
    productId: number;
    sku: string;
    productName: string;
    warehouseCode: string;
    quantity: number;
    reason: string;
  }): Observable<Stock> {
    return this.http.post<Stock>(`${this.base}/admin/inventory/stocks`, body);
  }

  adminAdjustStock(id: number, operation: 'increase' | 'decrease', quantity: number, reason: string): Observable<Stock> {
    return this.http.post<Stock>(`${this.base}/admin/inventory/stocks/${id}/${operation}`, { quantity, reason });
  }

  adminSetStock(id: number, quantity: number, reason: string): Observable<Stock> {
    return this.http.patch<Stock>(`${this.base}/admin/inventory/stocks/${id}/adjust`, { quantity, reason });
  }

  adminOrders(): Observable<Page<Order>> {
    return this.http.get<Page<Order>>(`${this.base}/admin/orders`, { params: { page: 0, size: 100 } });
  }

  adminUpdateOrderStatus(id: number, status: string, reason: string): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/admin/orders/${id}/status`, { status, reason });
  }

  adminPayments(): Observable<Page<Payment>> {
    return this.http.get<Page<Payment>>(`${this.base}/admin/payments`, { params: { page: 0, size: 100 } });
  }

  adminMockPaymentSuccess(reference: string, gatewayReference: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/admin/payments/${reference}/mock-success`, { gatewayReference });
  }

  adminMockPaymentFailure(reference: string, failureReason: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/admin/payments/${reference}/mock-failure`, { failureReason });
  }

  adminRefund(reference: string, amount: number, reason: string): Observable<unknown> {
    return this.http.post(`${this.base}/admin/payments/${reference}/refunds`, { amount, reason });
  }

  adminShipments(): Observable<Page<Shipment>> {
    return this.http.get<Page<Shipment>>(`${this.base}/admin/shipments`, { params: { page: 0, size: 100 } });
  }

  adminUpdateShipmentDetails(id: number, carrier: string, trackingNumber: string): Observable<Shipment> {
    return this.http.put<Shipment>(`${this.base}/admin/shipments/${id}/details`, { carrier, trackingNumber });
  }

  adminUpdateShipmentStatus(id: number, status: string, note: string): Observable<Shipment> {
    return this.http.patch<Shipment>(`${this.base}/admin/shipments/${id}/status`, { status, note });
  }
}
