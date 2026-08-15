import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { CartService } from '../../core/cart.service';
import { Category, Product } from '../../core/models';

@Component({
  selector: 'app-catalog',
  imports: [FormsModule, CurrencyPipe],
  templateUrl: './catalog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CatalogComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly cart = inject(CartService);

  readonly products = signal<Product[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly loading = signal(true);
  readonly message = signal('');
  readonly totalPages = signal(0);
  readonly currentPage = signal(0);
  search = '';
  categoryId?: number;

  ngOnInit(): void {
    this.api.getCategories().subscribe({
      next: categories => this.categories.set(categories),
      error: () => this.message.set('Categories are temporarily unavailable.')
    });
    this.loadProducts();
  }

  loadProducts(page = 0): void {
    this.loading.set(true);
    this.message.set('');
    this.api.getProducts(this.search, this.categoryId, page)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: result => {
          this.products.set(result.content);
          this.currentPage.set(result.number);
          this.totalPages.set(result.totalPages);
        },
        error: () => this.message.set('The catalog could not be loaded. Check that the gateway and product service are running.')
      });
  }

  clearFilters(): void {
    this.search = '';
    this.categoryId = undefined;
    this.loadProducts();
  }

  addToCart(product: Product): void {
    this.message.set('Checking stock…');
    this.api.getAvailability(product.id).subscribe({
      next: stock => {
        if (!stock.available || stock.availableQuantity < 1) {
          this.message.set(`${product.name} is currently out of stock.`);
          return;
        }
        this.cart.add(product);
        this.message.set(`${product.name} was added to your cart.`);
      },
      error: () => this.message.set(`No inventory record exists for ${product.name}. Ask an admin to add stock.`)
    });
  }
}
