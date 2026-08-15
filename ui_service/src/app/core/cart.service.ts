import { Injectable, computed, signal } from '@angular/core';
import { CartItem, Product } from './models';

const STORAGE_KEY = 'karan-commerce-cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly state = signal<CartItem[]>(this.restore());
  readonly items = this.state.asReadonly();
  readonly count = computed(() => this.state().reduce((sum, item) => sum + item.quantity, 0));
  readonly subtotal = computed(() => this.state().reduce(
    (sum, item) => sum + Number(item.product.price) * item.quantity,
    0
  ));

  add(product: Product): void {
    const existing = this.state().find(item => item.product.id === product.id);
    const next = existing
      ? this.state().map(item => item.product.id === product.id ? { ...item, quantity: item.quantity + 1 } : item)
      : [...this.state(), { product, quantity: 1 }];
    this.save(next);
  }

  setQuantity(productId: number, quantity: number): void {
    if (quantity <= 0) {
      this.remove(productId);
      return;
    }
    this.save(this.state().map(item => item.product.id === productId ? { ...item, quantity } : item));
  }

  remove(productId: number): void {
    this.save(this.state().filter(item => item.product.id !== productId));
  }

  clear(): void {
    this.save([]);
  }

  private save(items: CartItem[]): void {
    this.state.set(items);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }

  private restore(): CartItem[] {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') as CartItem[];
    } catch {
      return [];
    }
  }
}
