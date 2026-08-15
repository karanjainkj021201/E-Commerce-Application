import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { CartService } from './core/cart.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  readonly auth = inject(AuthService);
  readonly cart = inject(CartService);
  readonly menuOpen = signal(false);

  login(): void {
    void this.auth.login();
  }

  register(): void {
    void this.auth.register();
  }

  logout(): void {
    void this.auth.logout();
  }
}
